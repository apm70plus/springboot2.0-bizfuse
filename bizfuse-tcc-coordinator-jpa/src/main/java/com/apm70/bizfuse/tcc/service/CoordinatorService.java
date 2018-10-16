package com.apm70.bizfuse.tcc.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apm70.bizfuse.event.EventDrivenPublisher;
import com.apm70.bizfuse.tcc.domain.Participant;
import com.apm70.bizfuse.tcc.domain.TccStatus;
import com.apm70.bizfuse.tcc.repository.ParticipantRepository;
import com.apm70.bizfuse.util.Jacksons;

import lombok.extern.slf4j.Slf4j;

/**
 * TCC分布式事务协调服务，负责集中管理事务参与者，执行分布式事务的成功确认、失败补偿、超时补偿等逻辑
 * 
 * @author liuyg
 */
@Slf4j
@Service
public class CoordinatorService {

	@Autowired
	private ParticipantRepository participantRepository;
	@Autowired
	private EventDrivenPublisher eventDrivenPublisher;

	@Transactional(rollbackFor = Exception.class)
	public void processParticipant(Participant participant) {
		TccStatus tccStatus = participant.getTccStatus();
		if (TccStatus.TO_BE_CONFIRMED == tccStatus) {
			this.addTccTry(participant);
			return;
		}
		List<Participant> participants = participantRepository.findAllByTccId(participant.getTccId());
		// 如果已经有cancel的参与者，说明已经执行过cancel处理，未处理的参与者统一执行cancel处理
		boolean alredyCanceled = participants.stream().anyMatch(p -> {
			return p.getTccStatus() == TccStatus.CANCELED;
		});
		if (alredyCanceled) {
			participants.stream().filter(p -> p.getTccStatus() != TccStatus.CANCELED).forEach(this::tccCancel);
			return;
		}

		if (TccStatus.CONFIRMED == tccStatus) {// 执行确认处理
			participants.stream().filter(p -> p.getTccStatus() != TccStatus.CONFIRMED).forEach(this::tccConfirm);
		} else {// 执行撤销处理
			participants.stream().filter(p -> p.getTccStatus() != TccStatus.CANCELED).forEach(this::tccCancel);
		}
	}

	/**
	 * 本资源回收策略为定时轮询数据库, 存在资源竞争与重复计算的嫌疑, 待后续版本优化
	 */
	@Scheduled(fixedRate = 5000)
	@Transactional(rollbackFor = Exception.class)
	public void autoCancelTrying() {
		// 获取过期的资源
		final List<String> timeoutTccs = participantRepository.selectExpireReservation(10);
		for (String tccId : timeoutTccs) {
			List<Participant> participants = participantRepository.findAllByTccId(tccId);
			// 如果已经存在确认逻辑，可能是时间差导致confirm消息先于try消息到达，执行确认处理
			boolean needConfirm = participants.stream().filter(p -> p.getTccStatus() != TccStatus.TO_BE_CONFIRMED).allMatch(p -> p.getTccStatus() == TccStatus.CONFIRMED);
			if (needConfirm) {
				participants.stream().filter(p -> p.getTccStatus() == TccStatus.TO_BE_CONFIRMED).forEach(this::tccConfirm);
			} else {
			    // 所有未执行cancel的参与者，执行超时导致的“撤销”处理
			    participants.stream().filter(p -> p.getTccStatus() != TccStatus.CANCELED).forEach(this::tccTimeout);
			}
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void addTccTry(Participant participant) {
		try {
			// 数据库表索引支持幂等性，可能发生索引重复异常
			participantRepository.save(participant);
		} catch (Exception e) {
			log.info("重复的Tcc Participant 注册：{}", Jacksons.parse(participant));
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void tccConfirm(Participant participant) {
		// 通知TCC参与者，执行“确认”处理
		updateTccStatus(participant, TccStatus.CONFIRMED);
		notifyParticipant(participant);
	}

	@Transactional(rollbackFor = Exception.class)
	public void tccCancel(Participant participant) {
		// 通知所有TCC参与者，执行“撤销”处理
		updateTccStatus(participant, TccStatus.CANCELED);
		notifyParticipant(participant);
	}

	@Transactional(rollbackFor = Exception.class)
	public void tccTimeout(Participant participant) {
		// 通知所有TCC参与者，执行“撤销”处理
		participant.setErrorCode("timeout");
		updateTccStatus(participant, TccStatus.CANCELED);
		notifyParticipant(participant);
	}

	private void updateTccStatus(Participant participant, TccStatus status) {
		participant.setTccStatus(status);
		participantRepository.save(participant);
	}

	/*
	 * 通知TCC参与者执行“确认”或“撤销”操作
	 */
	private void notifyParticipant(Participant participant) {
		// businessType 规则 {controllerName}${routingKey}
		String businessType = participant.getCallbackURI();
		eventDrivenPublisher.publish(participant, businessType);
	}
}

package com.apm70.bizfuse.tcc.aspect;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apm70.bizfuse.event.EventDrivenPublisher;
import com.apm70.bizfuse.identity.GuidGenerator;
import com.apm70.bizfuse.tcc.config.Constants;
import com.apm70.bizfuse.tcc.model.Participant;
import com.apm70.bizfuse.tcc.model.TccStatus;
import com.apm70.bizfuse.tcc.util.TccContextHolder;
import com.apm70.bizfuse.web.utils.ServletContextHolder;

/**
 * TCC分布式事务参与者的客户端
 * 
 * @author liuyg
 */
@Service
public class TccParticipantClient {

	
	@Autowired
	public EventDrivenPublisher eventDrivenPublisher;
	
	static {
		EventDrivenPublisher.registerType(Constants.TCC_BUSINESS_TYPE, Constants.TCC_DIRECT_EXCHANGE, Constants.TCC_ROUTING_KEY);
	}
	
	public void initTccId() throws Throwable {
		String tccId = ServletContextHolder.getRequest().getHeader(Constants.TCC_ID_HEADER);
		if (tccId == null) {
			tccId = GuidGenerator.generate();
		}
		TccContextHolder.setTccId(tccId);
	}
	
	@Transactional
	public void registerTccParticipant(String tccCallbackURI) {
		Participant needConfirm = new Participant();
		needConfirm.setTccId(TccContextHolder.getTccId());
		needConfirm.setCallbackURI(tccCallbackURI);
		needConfirm.setExecuteTime(LocalDateTime.now());
		needConfirm.setExpireTime(LocalDateTime.now().plusSeconds(20));
		needConfirm.setTccStatus(TccStatus.TO_BE_CONFIRMED);
		publishEvent(needConfirm);
	}
	
	@Transactional
	public void executeTccCancel(String errorCode, String tccCallbackURI) {
		Participant needCancel = new Participant();
		needCancel.setTccId(TccContextHolder.getTccId());
		needCancel.setCallbackURI(tccCallbackURI);
		needCancel.setExecuteTime(LocalDateTime.now());
		needCancel.setExpireTime(LocalDateTime.now().plusSeconds(20));
		needCancel.setTccStatus(TccStatus.CANCELED);
		needCancel.setErrorCode(errorCode);
		publishEvent(needCancel);
	}
	
	@Transactional
	public void executeTccConfirm(String tccCallbackURI) {
		Participant confirm = new Participant();
		confirm.setTccId(TccContextHolder.getTccId());
		confirm.setCallbackURI(tccCallbackURI);
		confirm.setExecuteTime(LocalDateTime.now());
		confirm.setExpireTime(LocalDateTime.now().plusSeconds(20));
		confirm.setTccStatus(TccStatus.CONFIRMED);
		publishEvent(confirm);
	}

	private void publishEvent(Participant participant) {
		eventDrivenPublisher.publish(participant, Constants.TCC_BUSINESS_TYPE);
	}
}

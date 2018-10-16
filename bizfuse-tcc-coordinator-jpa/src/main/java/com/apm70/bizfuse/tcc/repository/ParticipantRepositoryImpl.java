package com.apm70.bizfuse.tcc.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.apm70.bizfuse.jpa.repository.QueryRepository;
import com.apm70.bizfuse.tcc.domain.Participant;
import com.apm70.bizfuse.tcc.domain.QParticipant;
import com.apm70.bizfuse.tcc.domain.TccStatus;

public class ParticipantRepositoryImpl extends QueryRepository implements ParticipantRepositoryCustom {

	@Override
	public List<String> selectExpireReservation(int limit) {
		QParticipant QP = QParticipant.participant;
		return this.query().selectDistinct(QP.tccId).from(QP)
				.where(QP.tccStatus.eq(TccStatus.TO_BE_CONFIRMED).and(QP.expireTime.before(LocalDateTime.now())))
				.fetch();
	}

	@Override
	protected Class<?> getModelClass() {
		return Participant.class;
	}

}

package com.apm70.bizfuse.tcc.repository;

import java.util.List;

public interface ParticipantRepositoryCustom {

	List<String> selectExpireReservation(int limit);
}

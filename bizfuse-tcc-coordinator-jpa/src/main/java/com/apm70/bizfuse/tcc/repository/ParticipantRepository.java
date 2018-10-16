package com.apm70.bizfuse.tcc.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.apm70.bizfuse.tcc.domain.Participant;

public interface ParticipantRepository extends CrudRepository<Participant, Long>, ParticipantRepositoryCustom {

	List<Participant> findAllByTccId(String tccId);

}
package com.apm70.bizfuse.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.apm70.bizfuse.domain.PublishedEvent;
import com.apm70.bizfuse.enums.EventStatus;

public interface PublishedEventRepository extends CrudRepository<PublishedEvent, Long> {

	List<PublishedEvent> findTop100ByEventStatus(EventStatus status);

	List<PublishedEvent> findTop100ByEventStatusAndLastModifiedDateBefore(
			EventStatus pending,
			LocalDateTime localDateTime);

}

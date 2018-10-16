package com.apm70.bizfuse.repository;

import org.springframework.data.repository.CrudRepository;

import com.apm70.bizfuse.domain.SubscribedEvent;

public interface SubscribedEventRepository extends CrudRepository<SubscribedEvent, Long> {

}

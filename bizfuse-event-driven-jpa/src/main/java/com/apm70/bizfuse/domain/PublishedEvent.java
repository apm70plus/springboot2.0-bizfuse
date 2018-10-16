package com.apm70.bizfuse.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.apm70.bizfuse.enums.EventStatus;
import com.apm70.bizfuse.jpa.domain.AuditEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

/**
 * 已发布的消息
 * 
 * @author liuyg
 *
 */
@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler", "fieldHandler"}, ignoreUnknown = true)
@Entity
public class PublishedEvent extends AuditEntity {

    @NotBlank
    @Column(length=128, nullable=false)
    private String businessType;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus;

    @NotBlank
    @JsonRawValue
    @Column(length=1024, nullable=false)
    private String payload;

    @NotBlank
    @Column(length=64, nullable=false)
    private String guid;

}
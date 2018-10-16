package com.apm70.bizfuse.tcc.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.apm70.bizfuse.jpa.domain.BasicEntity;
import com.apm70.bizfuse.web.jackson.LocalDateTimeToIso8601Serializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * TCC事务参与者。一次分布式TCC事务的所有参与者，具有唯一的tccId
 * @author
 */
@Getter
@Setter
@Entity
@Table(uniqueConstraints= {@UniqueConstraint(columnNames= {"tccId", "callbackURI"}, name="UniqueIndex_TccId_URI")})
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler", "fieldHandler"}, ignoreUnknown = true)
public class Participant extends BasicEntity {

    @ApiModelProperty(value = "TCC事务唯一标示", required = true, example = "UUID")
    @NotBlank
    @Column(nullable=false, length=64)
    private String tccId;
    
    @ApiModelProperty(value = "回调路径", required = true, example = "{businessType}${routingKey}")
    @NotBlank
    @Column(nullable=false, length=128)
    private String callbackURI;

    @ApiModelProperty(value = "过期时间, ISO标准", required = true, example = "2017-03-20T14:00:41")
    @JsonSerialize(using = LocalDateTimeToIso8601Serializer.class)
    private LocalDateTime expireTime;

    @ApiModelProperty(value = "发起confirm的时间, ISO标准", hidden = true, example = "2017-03-20T14:00:41")
    @JsonSerialize(using = LocalDateTimeToIso8601Serializer.class)
    private LocalDateTime executeTime;

    @ApiModelProperty(value = "参与方返回的错误码", hidden = true, example = "451")
    @Column(length=128)
    private String errorCode;

    @ApiModelProperty(value = "预留资源的状态", hidden = true, example = "TO_BE_CONFIRMED")
    @Enumerated(EnumType.ORDINAL)
    private TccStatus tccStatus = TccStatus.TO_BE_CONFIRMED;
    
    @CreatedDate
	private LocalDateTime createdDate;

	@LastModifiedDate
	private LocalDateTime lastModifiedDate;
}

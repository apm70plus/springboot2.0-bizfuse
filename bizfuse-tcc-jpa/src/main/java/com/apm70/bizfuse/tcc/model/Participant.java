package com.apm70.bizfuse.tcc.model;

import java.io.Serializable;
import java.time.LocalDateTime;

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
 * @author
 */
@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler", "fieldHandler"}, ignoreUnknown = true)
public class Participant implements Serializable {

    private static final long serialVersionUID = -1896632687832278753L;

    @ApiModelProperty(value = "TCC事务唯一标示", required = true, example = "UUID")
    private String tccId;
    
    @ApiModelProperty(value = "资源URI", required = true, example = "http://www.example.com/part/123")
    private String callbackURI;

    @ApiModelProperty(value = "过期时间, ISO标准", required = true, example = "2017-03-20T14:00:41")
    @JsonSerialize(using = LocalDateTimeToIso8601Serializer.class)
    private LocalDateTime expireTime;

    @ApiModelProperty(value = "发起confirm的时间, ISO标准", hidden = true, example = "2017-03-20T14:00:41")
    @JsonSerialize(using = LocalDateTimeToIso8601Serializer.class)
    private LocalDateTime executeTime;

    @ApiModelProperty(value = "参与方返回的错误码", hidden = true, example = "451")
    private String errorCode;

    @ApiModelProperty(value = "预留资源的状态", hidden = true, example = "TO_BE_CONFIRMED")
    private TccStatus tccStatus = TccStatus.TO_BE_CONFIRMED;
}

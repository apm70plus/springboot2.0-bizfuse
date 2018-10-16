package com.apm70.bizfuse.event;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.apm70.bizfuse.util.KeepAsJsonDeserialzier;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 业务消息的包装类，事件驱动型框架内部传输数据的数据结构，不对业务暴露
 * 
 * @author liuyg
 *
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler", "fieldHandler" }, ignoreUnknown = true)
public class Event implements Serializable {
	private static final long serialVersionUID = 2840081172778887899L;

	@NotNull
	@NotBlank
	private String businessType;

	@NotNull
	@NotBlank
	@JsonRawValue
	private String payload;

	@JsonDeserialize(using = KeepAsJsonDeserialzier.class)
	public void setPayload(String payload) {
		this.payload = payload;
	}

	@NotNull
	@NotBlank
	private String guid;

}
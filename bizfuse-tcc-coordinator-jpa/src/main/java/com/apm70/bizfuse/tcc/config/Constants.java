package com.apm70.bizfuse.tcc.config;

public final class Constants {

	private Constants() {}
	
	/**
	 * MQ TCC类型业务消息的交换机
	 */
	public static final String TCC_DIRECT_EXCHANGE = "d.tcc";
	/**
	 * MQ TCC类业务的消息类型
	 */
	public static final String TCC_BUSINESS_TYPE = "TCC-COORDINATOR";
}

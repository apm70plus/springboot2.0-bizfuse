package com.apm70.bizfuse.tcc.config;

public final class Constants {

	private Constants() {}
	
	/**
	 * MQ TCC类型业务消息的交换机
	 */
	public static final String TCC_DIRECT_EXCHANGE = "d.tcc";
	/**
	 * MQ TCC协调服务的消息类型
	 */
	public static final String TCC_BUSINESS_TYPE = "TCC-COORDINATOR";
	/**
	 * MQ TCC协调服务的消息路由
	 */
	public static final String TCC_ROUTING_KEY = "t666fb88-4cc2-11e7-9226-0242ac130004";
	/**
	 * TCC “死”消息的队列
	 */
    public static final String TCC_DEAD_QUEUE = "q.tcc.coordinator.dead";
    /**
	 * TCC “死”消息队列的路由KEY
	 */
    public static final String DEAD_TCC_ROUTING_KEY = "d666fb88-4cc2-11e7-9226-0242ac130004";
	/**
	 * TCCID在HttpRequest中的Header名
	 */
	public static final String TCC_ID_HEADER = "TCCID";
	
}

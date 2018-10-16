package com.apm70.bizfuse.tcc.domain;

/**
 * TCC事务状态
 * 
 * @author 
 */
public enum TccStatus {
	/** 等待确认 */
	TO_BE_CONFIRMED, 
	/** 已确认 */
	CONFIRMED, 
	/** 超时 */
	TIMEOUT, 
	/** 事务补偿 */
	CANCELED;
}

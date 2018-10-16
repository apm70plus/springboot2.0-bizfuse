package com.apm70.bizfuse.tcc.util;

import com.apm70.bizfuse.web.utils.ServletContextHolder;

public final class TccContextHolder {

	private static final String TCC_ID = "TCCID";
	
	private TccContextHolder() {}
	
	public static void setTccId(String tccId) {
		ServletContextHolder.getRequest().setAttribute(TCC_ID, tccId);
	}
	
	public static String getTccId() {
		return (String)ServletContextHolder.getRequest().getAttribute(TCC_ID);
	}
}

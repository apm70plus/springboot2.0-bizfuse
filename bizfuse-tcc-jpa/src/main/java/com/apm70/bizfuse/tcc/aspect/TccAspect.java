package com.apm70.bizfuse.tcc.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apm70.bizfuse.event.EventDrivenPublisher;
import com.apm70.bizfuse.event.EventHandler;
import com.apm70.bizfuse.web.utils.ServletContextHolder;

/**
 * TCC框架的ASPECT，负责织入TCC功能到TCC-Controller
 * 
 * @author liuyg
 */
@Aspect
@Component
public class TccAspect {

	private static final String TCC_ID_HEADER = "TCCID";

	@Autowired
	public TccParticipantClient tccParticipantClient;
	@Autowired
	public EventDrivenPublisher eventDrivenPublisher;

	@Pointcut(value = "@annotation(com.apm70.bizfuse.tcc.annotation.TccTransaction)")
	public void tccTryPointcut() {
	}
	
//	@Around(value = "tccTryPointcut()")
//	public Object arountTccTry(ProceedingJoinPoint joinPoint) throws Throwable {
//		tccParticipantClient.initTccId();
//		try {
//			return joinPoint.proceed();
//		} catch (Throwable ex) {
//			
//		}
//	}

	@Before(value = "tccTryPointcut()")
	public void beforeTccTry() throws Throwable {
		tccParticipantClient.initTccId();
	}

	@AfterReturning(value = "tccTryPointcut()")
	public void afterTccTry(JoinPoint joinPoint) {
		Object target = joinPoint.getTarget();
		String tccCallbackURI = ((EventHandler)target).getBusinessType();
		tccParticipantClient.registerTccParticipant(tccCallbackURI);
		if (isTccRoot()) {// tcc事务已经正常结束
			tccParticipantClient.executeTccConfirm(tccCallbackURI);
		}
	}

	@AfterThrowing(value = "tccTryPointcut()", throwing = "e")
	public void afterThrowing(JoinPoint joinPoint, Throwable e) {
		Object target = joinPoint.getTarget();
		String tccCallbackURI = ((EventHandler)target).getBusinessType();
		tccParticipantClient.executeTccCancel(e.getMessage(), tccCallbackURI);
	}

	private boolean isTccRoot() {
		return ServletContextHolder.getRequest().getHeader(TCC_ID_HEADER) == null;
	}
}

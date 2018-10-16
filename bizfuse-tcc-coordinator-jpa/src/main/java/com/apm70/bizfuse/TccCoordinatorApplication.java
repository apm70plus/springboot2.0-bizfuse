package com.apm70.bizfuse;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.apm70.bizfuse.web.annotation.EnableBizfuseWebMVC;

@EnableScheduling
@EnableJpaAuditing
@EnableBizfuseWebMVC
//@EnableAspectJAutoProxy(proxyTargetClass=true)
@SpringBootApplication
public class TccCoordinatorApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder().sources(TccCoordinatorApplication.class).run(args);
	}
}

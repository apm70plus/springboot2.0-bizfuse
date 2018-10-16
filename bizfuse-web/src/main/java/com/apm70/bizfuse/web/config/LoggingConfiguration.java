package com.apm70.bizfuse.web.config;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class LoggingConfiguration {

	@Value("${spring.application.name}")
	private String appName;

	@Value("${server.port}")
	private String serverPort;
	@Value("${logging.dailyrolling.enabled:false}")
	private boolean enableDailyRolling;

	private final Timer loggingRollingTimer = new Timer();

	@PostConstruct
	public void init() {

		if (this.enableDailyRolling) { // 启动定时器，每天零点输出一行日志
			Date startTime = new Date();
			startTime = DateUtils.addDays(startTime, 1);
			startTime = DateUtils.setHours(startTime, 0);
			startTime = DateUtils.setMinutes(startTime, 0);
			startTime = DateUtils.setSeconds(startTime, 1);
			this.loggingRollingTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					try {
						log.error("日志文件切换事件。");
					} catch (final Throwable e) {
					}
				}
			}, startTime, DateUtils.MILLIS_PER_DAY);
			log.info(Constants.CONFIG_LOG_MARK, "Started ErrorLog DailyRolling Task");
		}
	}

	@PreDestroy
	public void destroy() {
		if (this.enableDailyRolling) { // 启动定时器，每天零点输出一行日志
			this.loggingRollingTimer.cancel();
			log.info(Constants.CONFIG_LOG_MARK, "Stoped ErrorLog DailyRolling Task");
		}
	}
}

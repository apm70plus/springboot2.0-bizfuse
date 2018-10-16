package com.apm70.bizfuse.web.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@ConfigurationProperties(prefix = "bizfuse.web", ignoreUnknownFields = false)
public class BizfuseWebProperties {

	private final Swagger swagger = new Swagger();

	private final HttpClient httpClient = new HttpClient();

	private final Async async = new Async();

	private final List<String> corsPath = new ArrayList<>();

	@Getter
	@Setter
	public static class Swagger {
		private String title = "microapp API";
		private String description = "microapp API documentation";
		private String version = "0.0.1";
		private String termsOfServiceUrl;
		private String contactName;
		private String contactUrl;
		private String contactEmail;
		private String license;
		private String licenseUrl;
		private List<String> pathPatterns = new ArrayList<>();
	}

	@Getter
	@Setter
	public static class HttpClient {
		private int maxTotal = 100;
		private int maxPerRoute = 50;
		private int retryTimes = 3;
		private int connectTimeout = 20000;
		private int readTimeout = 60000;
		private int connectionRequestTimeout = 10000;
		private boolean bufferRequestBody = false;
	}

	@Getter
	@Setter
	public static class Async {
		private boolean enabled = false;
		private int corePoolSize = 2;
		private int maxPoolSize = 50;
		private int queueCapacity = 10000;
	}
}

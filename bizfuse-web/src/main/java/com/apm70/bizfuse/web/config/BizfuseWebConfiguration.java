package com.apm70.bizfuse.web.config;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import com.apm70.bizfuse.web.local.AngularCookieLocaleResolver;
import com.apm70.bizfuse.web.support.RequestBodyArgumentResolver;
import com.apm70.bizfuse.web.support.SearchableArgumentResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableConfigurationProperties({ BizfuseWebProperties.class })
@ComponentScan(basePackages = "com.apm70.bizfuse.web")
public class BizfuseWebConfiguration implements WebMvcConfigurer, ServletContextInitializer {

	@Autowired
	private Environment env;

	@Autowired
	private BizfuseWebProperties properties;

	@Autowired
	private ApplicationContext applicationContext;
	
//	@Bean
//	public UndertowServletWebServerFactory servletWebServerFactory() {
//		UndertowServletWebServerFactory factory = new UndertowServletWebServerFactory();
//		factory.addBuilderCustomizers(new UndertowBuilderCustomizer() {
//
//			@Override
//			public void customize(Builder builder) {
//				builder.addHttpListener(8080, "0.0.0.0");
//			}
//
//		});
//		return factory;
//	}

	@Override
	public void onStartup(final ServletContext servletContext) throws ServletException {
		if (this.env.getActiveProfiles().length != 0) {
		    log.info(Constants.CONFIG_LOG_MARK, "Web application configuration, using profiles: " +
					Arrays.toString(this.env.getActiveProfiles()));
		}
		if (this.env.acceptsProfiles(Constants.SPRING_PROFILE_DEVELOPMENT)) {
			this.initH2Console(servletContext);
		}
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		if (this.properties.getCorsPath().isEmpty()) {
			return;
		}
	    log.info(Constants.CONFIG_LOG_MARK, "CORS enabled");
		for (final String path : this.properties.getCorsPath()) {
			registry.addMapping(path);
		}
	}

	@Override
	public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new SearchableArgumentResolver());
		argumentResolvers.add(0, new RequestBodyArgumentResolver());
	}

	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		// 注册ExcelView解析器
		ViewResolver excelViewResolver = new ViewResolver() {
			private Map<String, AbstractXlsView> views = null;

			@Override
			public View resolveViewName(String viewName, Locale locale) throws Exception {
				AbstractXlsView xlsView = this.getXlsView(viewName);
				return xlsView;
			}

			private AbstractXlsView getXlsView(String viewName) {
				if (this.views == null) {
					this.views = BizfuseWebConfiguration.this.applicationContext.getBeansOfType(AbstractXlsView.class);
				}
				return this.views.get(viewName);
			}
		};
		registry.viewResolver(excelViewResolver);
		log.info(Constants.CONFIG_LOG_MARK, "ExcelViewResolver registerd");
	}

	@Override
	public void extendMessageConverters(final List<HttpMessageConverter<?>> converters) {
		// 配置数据库Date类型转json时，格式为时间戳Long
		//final SqlDateSerializer sqlDateSerializer = new SqlDateSerializer().withFormat(true, null);
		//final SimpleModule module = new SimpleModule();
		//module.addSerializer(Date.class, sqlDateSerializer);
		JavaTimeModule javaTimeModule = new JavaTimeModule();
		for (final HttpMessageConverter<?> converter : converters) {
			if (converter instanceof MappingJackson2HttpMessageConverter) {
				final MappingJackson2HttpMessageConverter jsonMessageConverter = (MappingJackson2HttpMessageConverter) converter;
				final ObjectMapper objectMapper = jsonMessageConverter.getObjectMapper();
				//objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
				objectMapper.registerModule(javaTimeModule);
			}
		}
		log.info(Constants.CONFIG_LOG_MARK, "JacksonMessageConvertor WRITE_DATES_AS_TIMESTAMPS configured");
	}

	/**
	 * Initializes H2 console.
	 */
	private void initH2Console(final ServletContext servletContext) {
		final ServletRegistration.Dynamic h2ConsoleServlet = servletContext.addServlet("H2Console",
				new org.h2.server.web.WebServlet());
		h2ConsoleServlet.addMapping("/h2-console/*");
		h2ConsoleServlet.setInitParameter("-properties", "src/main/resources/");
		h2ConsoleServlet.setLoadOnStartup(1);
		log.info(Constants.CONFIG_LOG_MARK, "H2 console enabled");
	}

	@Bean
	public HttpComponentsClientHttpRequestFactory httpClientFactory() {
		// 长连接保持30秒
		final PoolingHttpClientConnectionManager pollingConnectionManager = new PoolingHttpClientConnectionManager(30,
				TimeUnit.SECONDS);
		// 总连接数
		pollingConnectionManager.setMaxTotal(this.properties.getHttpClient().getMaxTotal());
		// 同路由的并发数
		pollingConnectionManager.setDefaultMaxPerRoute(this.properties.getHttpClient().getMaxPerRoute());

		final HttpClientBuilder httpClientBuilder = HttpClients.custom();
		httpClientBuilder.setConnectionManager(pollingConnectionManager);
		// 重试次数，默认是3次，没有开启
		httpClientBuilder.setRetryHandler(
				new DefaultHttpRequestRetryHandler(this.properties.getHttpClient().getRetryTimes(), true));
		// 保持长连接配置，需要在头添加Keep-Alive
		httpClientBuilder.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE);
		final List<Header> headers = new ArrayList<>();
		httpClientBuilder.setDefaultHeaders(headers);
		final HttpClient httpClient = httpClientBuilder.setRedirectStrategy(new RedirectStrategy() {

			@Override
			public boolean isRedirected(final HttpRequest request, final HttpResponse response,
					final HttpContext context) throws ProtocolException {
				return false;
			}

			@Override
			public HttpUriRequest getRedirect(final HttpRequest request, final HttpResponse response,
					final HttpContext context) throws ProtocolException {
				return null;
			}
		}).build();

		// httpClient连接配置，底层是配置RequestConfig
		final HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = 
				new HttpComponentsClientHttpRequestFactory(httpClient);
		// 连接超时
		clientHttpRequestFactory.setConnectTimeout(this.properties.getHttpClient().getConnectTimeout());
		// 数据读取超时时间，即SocketTimeout
		clientHttpRequestFactory.setReadTimeout(this.properties.getHttpClient().getReadTimeout());
		// 连接不够用的等待时间，不宜过长，必须设置，比如连接不够用时，时间过长将是灾难性的
		clientHttpRequestFactory
				.setConnectionRequestTimeout(this.properties.getHttpClient().getConnectionRequestTimeout());
		// 缓冲请求数据，默认值是true。通过POST或者PUT大量发送数据时，建议将此属性更改为false，以免耗尽内存。
		clientHttpRequestFactory.setBufferRequestBody(this.properties.getHttpClient().isBufferRequestBody());
		return clientHttpRequestFactory;
	}

	/**
	 * 带连接池的RestTemplate，用于微服务间通讯
	 * 
	 * @return
	 */
	@Bean("poolingRestTemplate")
	public RestTemplate poolingRestTemplate() {
		final RestTemplate restTemplate = new RestTemplate(this.httpClientFactory());
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
		log.info(Constants.CONFIG_LOG_MARK, "PoolingRestTemplate initialized, Redirect option was disabled");
		return restTemplate;
	}
	
	@Bean(name = "localeResolver")
    public LocaleResolver localeResolver() {
        final AngularCookieLocaleResolver cookieLocaleResolver = new AngularCookieLocaleResolver();
        cookieLocaleResolver.setCookieName("NG_TRANSLATE_LANG_KEY");
        return cookieLocaleResolver;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        final LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("language");
        registry.addInterceptor(localeChangeInterceptor);
    }
}

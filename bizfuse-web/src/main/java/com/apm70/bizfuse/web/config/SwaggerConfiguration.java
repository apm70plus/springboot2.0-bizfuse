package com.apm70.bizfuse.web.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Springfox Swagger configuration. Warning! When having a lot of REST
 * endpoints, Springfox can become a performance issue. In that case, you can
 * use a specific Spring profile for this class, so that only front-end
 * developers have access to the Swagger view.
 */
@Configuration
@EnableSwagger2
@Profile("swagger")
public class SwaggerConfiguration {

	private final Logger log = LoggerFactory.getLogger(SwaggerConfiguration.class);

	public static final String DEFAULT_INCLUDE_PATTERN = "/api/**";

	/**
	 * Swagger Springfox configuration.
	 *
	 * @param bizfuseWebProperties
	 *            the properties of the application
	 * @return the Swagger Springfox configuration
	 */
	@Bean
	public Docket swaggerSpringfoxDocket(final BizfuseWebProperties bizfuseWebProperties) {
		final Contact contact = new Contact(bizfuseWebProperties.getSwagger().getContactName(),
				bizfuseWebProperties.getSwagger().getContactUrl(), bizfuseWebProperties.getSwagger().getContactEmail());

		final ApiInfo apiInfo = new ApiInfo(bizfuseWebProperties.getSwagger().getTitle(),
				bizfuseWebProperties.getSwagger().getDescription(), bizfuseWebProperties.getSwagger().getVersion(),
				bizfuseWebProperties.getSwagger().getTermsOfServiceUrl(), contact,
				bizfuseWebProperties.getSwagger().getLicense(), bizfuseWebProperties.getSwagger().getLicenseUrl(),
				Collections.emptyList());

		Predicate<String> pathPattern = PathSelectors.ant(SwaggerConfiguration.DEFAULT_INCLUDE_PATTERN);
		final List<String> pathPatterns = bizfuseWebProperties.getSwagger().getPathPatterns();
		final List<Predicate<String>> p = new ArrayList<>();
		pathPatterns.stream().forEach(pattern -> {
			p.add(PathSelectors.ant(pattern.trim()));
		});
		if (!p.isEmpty()) {
			pathPattern = Predicates.or(p);
		}
		final Docket docket = new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo).forCodeGeneration(true)
				.genericModelSubstitutes(ResponseEntity.class).ignoredParameterTypes(java.sql.Date.class)
				.directModelSubstitute(java.time.LocalDate.class, java.sql.Date.class)
				.directModelSubstitute(java.time.ZonedDateTime.class, Date.class)
				//.ignoredParameterTypes(Pageable.class, PagedResourcesAssembler.class)
				.directModelSubstitute(java.time.LocalDateTime.class, Date.class).select().paths(pathPattern).build();
		this.log.info(Constants.CONFIG_LOG_MARK, "Swagger enabled");
		return docket;
	}
}

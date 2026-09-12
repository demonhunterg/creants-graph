package com.creants.graph.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMethod;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	
	@Bean
	public Docket api() {
		
		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.basePackage("com.creants.graph.controller"))
				.paths(PathSelectors.any()).build().apiInfo(apiInfo()).useDefaultResponseMessages(false)
				.globalResponseMessage(RequestMethod.GET, responseMessages())
		        .globalResponseMessage(RequestMethod.POST, responseMessages())
				.securityContexts(Collections.singletonList(securityContext()))
				.securitySchemes(Collections.singletonList(apiKey()))
				.ignoredParameterTypes(AuthenticationPrincipal.class);

	}

	private SecurityContext securityContext() {
		return SecurityContext.builder().securityReferences(defaultAuth()).forPaths(PathSelectors.regex("/api/v1.*"))
				.build();
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder().title("ACCOUNNT API").description(
				"This document is to describe how to login.")
				.version("1.0").termsOfServiceUrl("").licenseUrl("").build();
	}

	private ApiKey apiKey() {
		return new ApiKey("Authorization", "Authorization", "header");
	}

	List<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return Collections.singletonList(new SecurityReference("Authorization", authorizationScopes));
	}
	
	// @formatter:off
	private List<ResponseMessage> responseMessages() {
		return Arrays.asList(
				new ResponseMessageBuilder().code(500).message("Internal Server Error").build()
		);
	}
	// @formatter:on
}

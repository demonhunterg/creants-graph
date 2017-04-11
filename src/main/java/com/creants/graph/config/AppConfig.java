package com.creants.graph.config;

import javax.servlet.Filter;

import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.creants.graph.security.CultureFilter;

/**
 * @author LamHa
 *         https://spring.io/blog/2015/06/08/cors-support-in-spring-framework
 */
@Configuration
public class AppConfig extends WebMvcConfigurerAdapter {
	@Bean
	public EmbeddedServletContainerFactory servletContainer() {
		return new JettyEmbeddedServletContainerFactory();
	}


	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}


	@Bean
	public Filter cultureFilter() {
		return new CultureFilter();
	}


	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**");
	}

}

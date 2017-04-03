package com.creants.graph.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.creants.graph.security.JwtAuthenticationTokenFilter;

/**
 * @author LamHa
 *
 */
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		// JWT dont need CSRF
		httpSecurity.csrf().disable().exceptionHandling().and().sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests()
				.antMatchers("oauth/**").permitAll().and()
				.addFilterBefore(new JwtAuthenticationTokenFilter("user/**"), BasicAuthenticationFilter.class);

		// disable page caching
		httpSecurity.headers().cacheControl();
	}

}

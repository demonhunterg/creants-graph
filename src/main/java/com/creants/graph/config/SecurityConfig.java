package com.creants.graph.config;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.creants.graph.security.JwtAuthenticationEntryPoint;
import com.creants.graph.security.JwtAuthenticationProvider;
import com.creants.graph.security.JwtAuthenticationSuccessHandler;
import com.creants.graph.security.JwtAuthenticationTokenFilter;

/**
 * @author LamHa
 *         https://gitlab.com/palmapps/jwt-spring-security-demo/blob/master/src/main/java/nl/palmapps/myawesomeproject/security/config/WebSecurityConfig.java
 *
 */
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	private JwtAuthenticationEntryPoint unauthorizedHandler;

	@Autowired
	private JwtAuthenticationProvider authenticationProvider;

	@Bean
	@Override
	public AuthenticationManager authenticationManager() throws Exception {
		ArrayList<AuthenticationProvider> providers = new ArrayList<>();
		providers.add(authenticationProvider);
		return new ProviderManager(providers);
	}

	public JwtAuthenticationTokenFilter authenticationTokenFilterBean() throws Exception {
		JwtAuthenticationTokenFilter authenticationTokenFilter = new JwtAuthenticationTokenFilter("/user/**");
		authenticationTokenFilter.setAuthenticationManager(authenticationManager());
		authenticationTokenFilter.setAuthenticationSuccessHandler(new JwtAuthenticationSuccessHandler());
		return authenticationTokenFilter;
	}

	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		// JWT dont need CSRF
		httpSecurity.csrf().disable().exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests()
				.antMatchers("/oauth/**", "/account/**", "/internal/**").permitAll().anyRequest().authenticated();
		httpSecurity.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);

		// disable page caching
		httpSecurity.headers().cacheControl();
	}

}

package com.creants.graph.config;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.creants.graph.security.JwtAuthenticationEntryPoint;
import com.creants.graph.security.JwtAuthenticationProvider;
import com.creants.graph.security.JwtAuthenticationSuccessHandler;
import com.creants.graph.security.JwtAuthenticationTokenFilter;

/**
 * @author LamHa
 *
 */
@EnableWebSecurity
public class WebSecurityAdapter extends WebSecurityConfigurerAdapter {

	@Autowired
	private JwtAuthenticationEntryPoint unauthorizedHandler;

	@Autowired
	private JwtAuthenticationProvider authenticationProvider;

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}


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
	
	@Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*")); 
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Access-Control-Allow-Origin"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		// JWT dont need CSRF
		httpSecurity
		.headers().frameOptions().sameOrigin()
		.and()
        .cors()
        .and()
        .csrf().disable()
        .authorizeRequests()
				.antMatchers("/oauth/**", "/account/**", "/v2/api-docs", "/configuration/**", "/swagger*/**", "/webjars/**",
						"/api-docs/**", "/actuator/**", "/schedule/**", "/cache/**", "/internal/**")
				.permitAll()
        .antMatchers("/**").authenticated()
        .and()
        .exceptionHandling()
        .authenticationEntryPoint(unauthorizedHandler)
        .and()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);
	}

}

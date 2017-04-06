package com.creants.graph.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import com.creants.graph.security.exception.JwtTokenMissingException;
import com.creants.graph.security.model.JwtAuthenticationToken;
import com.creants.graph.security.util.AuthHelper;
import com.creants.graph.util.Tracer;

/**
 * @author LamHa
 *
 */
public class JwtAuthenticationTokenFilter extends AbstractAuthenticationProcessingFilter {

	public JwtAuthenticationTokenFilter(String defaultFilterProcessesUrl) {
		super(defaultFilterProcessesUrl);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {
		System.out.println("************** attemptAuthentication ********************");
		String header = request.getHeader("Authorization");
		if (header == null || !header.startsWith("Bearer ")) {
			throw new JwtTokenMissingException("No JWT token found in request headers");
		}

		try {
			String authToken = header.substring(7);
			AuthHelper.verifyToken(authToken);
			return getAuthenticationManager().authenticate(new JwtAuthenticationToken(authToken));
		} catch (Exception e) {
			Tracer.debug(this.getClass(), "attemptAuthentication fail!", Tracer.getTraceMessage(e));
			throw new ServletException("Invalid token.");
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		super.successfulAuthentication(request, response, chain, authResult);

		System.out.println("************** successfulAuthentication ************************");
		// As this authentication is in HTTP header, after success we need to
		// continue the request normally
		// and return the response as if the resource was not secured at all
		chain.doFilter(request, response);
	}

}

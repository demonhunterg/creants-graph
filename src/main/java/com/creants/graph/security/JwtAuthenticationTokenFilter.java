package com.creants.graph.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

/**
 * @author LamHa
 *         https://www.toptal.com/java/rest-security-with-jwt-spring-security-and-java
 *         https://gitlab.com/palmapps/jwt-spring-security-demo/blob/master/src/main/java/nl/palmapps/myawesomeproject/security/model/JwtAuthenticationToken.java
 *
 */
public class JwtAuthenticationTokenFilter extends AbstractAuthenticationProcessingFilter {

	public JwtAuthenticationTokenFilter(String defaultFilterProcessesUrl) {
		super(defaultFilterProcessesUrl);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {

		String token = request.getParameter("token");
		if (token == null || token.length() <= 0) {
			throw new ServletException("Missing or invalid Authorization header.");
		}

		try {
			AuthHelper.verifyToken(token);
			return getAuthenticationManager().authenticate(new JwtAuthenticationToken(token));
		} catch (Exception e) {
			throw new ServletException("Invalid token.");
		}
	}

}

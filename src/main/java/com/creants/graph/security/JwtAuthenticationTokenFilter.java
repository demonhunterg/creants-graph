package com.creants.graph.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import com.creants.graph.security.model.JwtAuthenticationToken;
import com.creants.graph.security.util.AuthHelper;

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
		System.out.println("************** attemptAuthentication ********************");
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
	
	@Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);

        // As this authentication is in HTTP header, after success we need to continue the request normally
        // and return the response as if the resource was not secured at all
        chain.doFilter(request, response);
    }

}

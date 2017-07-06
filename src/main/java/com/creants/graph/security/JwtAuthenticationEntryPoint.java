package com.creants.graph.security;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.creants.graph.service.MessageFactory;
import com.creants.graph.util.ErrorCode;
import com.creants.graph.util.Tracer;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author LamHa
 *
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {
	private static final long serialVersionUID = 1L;


	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {

		// Khi có lỗi xảy ra thực hiện trả về lỗi
		Tracer.error(this.getClass(), "JwtAuthenticationEntryPoint handle exception..................");
		// response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
		// "Unauthorized");

		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		ObjectMapper mapper = new ObjectMapper();
		String writeValueAsString = mapper
				.writeValueAsString(MessageFactory.createErrorMessage(ErrorCode.TOKEN_EXPIRED, "Token has exprired"));
		response.getOutputStream().println(writeValueAsString);

	}

}

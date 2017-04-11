package com.creants.graph.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.creants.graph.security.model.AuthenticatedUser;
import com.creants.graph.util.Security;
import com.creants.graph.util.Tracer;

/**
 * @author LamHM
 *
 */
public class CultureRequestWrapper extends HttpServletRequestWrapper {
	public CultureRequestWrapper(HttpServletRequest request) {
		super(request);
	}


	@Override
	public String[] getParameterValues(String name) {
		String requestURI = this.getRequestURI();
		// posted values a et b
		if (requestURI.startsWith("/user") && name != null && name.equals("data")) {
			UsernamePasswordAuthenticationToken userPrincipal = (UsernamePasswordAuthenticationToken) getUserPrincipal();
			AuthenticatedUser principal = (AuthenticatedUser) userPrincipal.getPrincipal();
			String[] values = super.getParameterValues(name);
			String[] newValues = values.clone();
			try {
				String privateKey = principal.getPrivateKey();
				newValues[0] = Security.decrypt(privateKey, newValues[0]);
				return newValues;
			} catch (Exception e) {
				Tracer.warn(this.getClass(),
						"Decrypt data fail! data: " + newValues[0] + ", user: " + principal.toString());
			}

			return null;
		}
		// other cases
		return super.getParameterValues(name);
	}
}

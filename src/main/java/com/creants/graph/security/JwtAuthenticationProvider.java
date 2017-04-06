package com.creants.graph.security;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.creants.graph.om.User;
import com.creants.graph.security.model.AuthenticatedUser;

/**
 * @author LamHa
 *
 */
@Component
public class JwtAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	@Override
	public boolean supports(Class<?> authentication) {
		return (JwtAuthenticationToken.class.isAssignableFrom(authentication));
	}

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
	}

	@Override
	protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
		System.out.println("retrie user");
		JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
		String token = jwtAuthenticationToken.getToken();

		User user = AuthHelper.getUser(token);

		List<GrantedAuthority> authorityList = AuthorityUtils.commaSeparatedStringToAuthorityList("USER");
		return new AuthenticatedUser(user.getId(), token, authorityList);
	}

}
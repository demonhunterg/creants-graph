package com.creants.graph.security.model;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author LamHa
 *
 */
public class AuthenticatedUser implements UserDetails {
	private static final long serialVersionUID = 1L;
	private final Integer userId;
	private final String token;
	private final Collection<? extends GrantedAuthority> authorities;

	public AuthenticatedUser(Integer userId, String token, Collection<? extends GrantedAuthority> authorities) {
		this.userId = userId;
		this.token = token;
		this.authorities = authorities;
	}

	@JsonIgnore
	public Integer getUserId() {
		return userId;
	}

	@Override
	@JsonIgnore
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isEnabled() {
		return true;
	}

	public String getToken() {
		return token;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return null;
	}
}

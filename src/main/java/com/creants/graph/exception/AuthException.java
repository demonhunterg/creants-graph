package com.creants.graph.exception;

/**
 * @author LamHM
 *
 */
public class AuthException extends CreantsException {
	private static final long serialVersionUID = 1L;


	public AuthException(int code, String message) {
		super(code, message);
	}

}

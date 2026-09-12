package com.creants.graph.exception;

import com.creants.graph.util.ErrorCode;

/**
 * @author LamHM
 *
 */
public class LoginException extends Exception {
	private static final long serialVersionUID = 1L;
	private ErrorCode code;


	public LoginException(ErrorCode code) {
		this.code = code;
	}


	public ErrorCode getCode() {
		return code;
	}

}

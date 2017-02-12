package com.creants.graph.exception;

/**
 * @author LamHa
 *
 */
public class CreantsException extends Exception {
	private static final long serialVersionUID = 1L;
	private int code;

	public CreantsException(int code, String message) {
		super(message);
		this.code = code;
	}

	public int getErrorCode() {
		return code;
	}

}

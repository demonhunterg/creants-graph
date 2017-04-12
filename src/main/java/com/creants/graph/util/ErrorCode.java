package com.creants.graph.util;

/**
 * @author LamHa
 *
 */
public enum ErrorCode {
	BAD_REQUEST(-1, "Bad request"),
	USER_NOT_FOUND(1000, "User not found"),
	INVALID_USERNAME(1001, "Invalid username. Account name must be between 6 and 18 characters"),
	INVALID_PASSWORD(1002, "Invalid password. Password must be between 3 and 18 characters"),
	INVALID_EMAIL(1003, "Invalid email"),
	INVALID_VERIFY_CODE(1004, "Invalid verify code"),
	VERIFY_CODE_EXPIRED(1005, "Verify code expired"),
	EXIST_USER(1006, "Exist user"),
	TOKEN_EXPIRED(1008, "Token expired"),
	UPDATE_FAIL(1009, "Update fail");

	public int id;
	public String message;


	ErrorCode(int id, String message) {
		this.id = id;
		this.message = message;
	}


	public int getId() {
		return id;
	}


	public String getMessage() {
		return message;
	}

}

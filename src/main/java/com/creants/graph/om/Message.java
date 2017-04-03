package com.creants.graph.om;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

/**
 * @author LamHa
 *
 */
public class Message extends PropertyNamingStrategy {
	private static final long serialVersionUID = 1L;
	private int code;
	@JsonInclude(Include.NON_NULL)
	private Object data;
	private String msg;
	@JsonInclude(Include.NON_NULL)
	private String token;
	@JsonInclude(Include.NON_NULL)
	private String privateKey;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

}

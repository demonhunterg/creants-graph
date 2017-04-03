package com.creants.graph.service;

import com.creants.graph.om.Message;

/**
 * @author LamHa
 *
 */
public class MessageFactory {

	public static Message createErrorMessage(int errorCode, String message) {
		Message msg = new Message();
		msg.setCode(errorCode);
		msg.setMsg(message);
		return msg;
	}

	public static Message createMessage(Object data) {
		Message msg = new Message();
		msg.setCode(1);
		msg.setMsg("Success");
		if (data != null) {
			msg.setData(data);
		}
		return msg;
	}
}

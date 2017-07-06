package com.creants.graph.controller;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.couchbase.client.java.document.json.JsonObject;
import com.creants.graph.dao.IUserRepository;
import com.creants.graph.om.Message;
import com.creants.graph.om.User;
import com.creants.graph.security.model.AuthenticatedUser;
import com.creants.graph.security.util.AuthHelper;
import com.creants.graph.service.MessageFactory;
import com.creants.graph.util.ErrorCode;
import com.creants.graph.util.Security;
import com.creants.graph.util.Tracer;

/**
 * @author LamHa
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {
	@Autowired
	private IUserRepository userRepository;


	@PostMapping(path = "get", produces = "application/json; charset=UTF-8")
	public @ResponseBody Message getUserInfo(@AuthenticationPrincipal AuthenticatedUser authUser) {
		User user = userRepository.getUserInfo(authUser.getUserId());
		if (user == null) {
			return MessageFactory.createErrorMessage(ErrorCode.USER_NOT_FOUND);
		}

		return MessageFactory.createMessage(user);
	}


	@PostMapping(path = "update", produces = "application/json; charset=UTF-8")
	public @ResponseBody Message updateUserInfo(@AuthenticationPrincipal AuthenticatedUser authUser,
			@RequestParam String data) {

		JsonObject jo = JsonObject.fromJson(data);
		int result = userRepository.updateUserInfo(authUser.getUserId(), jo.getString("fullname"), jo.getInt("gender"),
				jo.getString("location"), jo.getString("birthday"));
		if (result != 1) {
			return MessageFactory.createErrorMessage(ErrorCode.UPDATE_FAIL);
		}

		return MessageFactory.createMessage(null);
	}


	@PostMapping(path = "change/pw", produces = "application/json; charset=UTF-8")
	public @ResponseBody Message changePassword(@AuthenticationPrincipal AuthenticatedUser authUser,
			@RequestParam String data) {

		JsonObject jo = JsonObject.fromJson(data);
		if (jo == null) {
			return MessageFactory.createErrorMessage(ErrorCode.LACK_OF_INFO);
		}

		String password = jo.getString("password");
		String newPassword = jo.getString("new_password");
		String reNewPassword = jo.getString("re_new_password");
		if (password == null || newPassword == null || reNewPassword == null) {
			return MessageFactory.createErrorMessage(ErrorCode.LACK_OF_INFO);
		}

		if (!newPassword.equals(reNewPassword)) {
			return MessageFactory.createErrorMessage(ErrorCode.PASSWORD_NOT_MATCH);
		}

		newPassword = newPassword.trim();
		if (newPassword.length() < 6) {
			return MessageFactory.createErrorMessage(ErrorCode.INVALID_PASSWORD);
		}

		try {
			int result = userRepository.changePassword(authUser.getUserId(), Security.encryptMD5(password),
					Security.encryptMD5(newPassword));
			if (result != 1) {
				return MessageFactory.createErrorMessage(ErrorCode.WRONG_PASSWORD);
			}
		} catch (NoSuchAlgorithmException e) {
			Tracer.error(this.getClass(), "Change password fail!");
			return MessageFactory.createErrorMessage(ErrorCode.INVALID_PASSWORD);
		}

		return MessageFactory.createMessage(null);
	}


	@PostMapping(path = "signout", produces = "application/json;charset=UTF-8")
	public @ResponseBody Message signout(@AuthenticationPrincipal AuthenticatedUser authUser) {
		Tracer.debug(this.getClass(), "call signout: " + authUser.getUsername() + "/userId: " + authUser.getUserId());
		return MessageFactory.createMessage(null);
	}


	@PostMapping(path = "validate", produces = "application/json;charset=UTF-8")
	public @ResponseBody Message validateToken(@AuthenticationPrincipal AuthenticatedUser authUser) {
		String token = authUser.getToken();
		User user = null;
		try {
			AuthHelper.verifyToken(token);
			user = userRepository.getUserInfo(authUser.getUserId());
			if (user == null) {
				return MessageFactory.createErrorMessage(ErrorCode.TOKEN_EXPIRED);
			}
		} catch (Exception e1) {
			return MessageFactory.createErrorMessage(ErrorCode.TOKEN_EXPIRED);
		}

		return responseMessage(user, token, authUser.getPrivateKey());
	}


	private Message responseMessage(User user, String token, String privateKey) {
		Map<String, Object> data = new HashMap<>();
		data.put("user", user);

		Message message = MessageFactory.createMessage(data);
		message.setToken(token);
		message.setPrivateKey(Security.genPrivateKey(token, user.getUserId()));
		return message;
	}

}

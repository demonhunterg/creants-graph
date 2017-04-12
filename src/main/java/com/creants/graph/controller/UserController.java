package com.creants.graph.controller;

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


	@PostMapping(path = "signout", produces = "application/json;charset=UTF-8")
	public @ResponseBody Message signout(@AuthenticationPrincipal AuthenticatedUser authUser) {
		return MessageFactory.createMessage(null);
	}


	@PostMapping(path = "validate", produces = "application/json;charset=UTF-8")
	public @ResponseBody Message validateToken(@AuthenticationPrincipal AuthenticatedUser authUser) {
		try {
			AuthHelper.verifyToken(authUser.getToken());
		} catch (Exception e1) {
			return MessageFactory.createErrorMessage(ErrorCode.TOKEN_EXPIRED);
		}

		return MessageFactory.createMessage(null);
	}

}

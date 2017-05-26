package com.creants.graph.controller;

import java.security.NoSuchAlgorithmException;

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
import com.creants.graph.service.MessageFactory;
import com.creants.graph.util.ErrorCode;
import com.creants.graph.util.Security;
import com.creants.graph.util.Tracer;

/**
 * @author LamHa
 *
 */
@RestController
@RequestMapping("/profile")
public class ProfileController {
	@Autowired
	private IUserRepository userRepository;


	@PostMapping(path = "get", produces = "application/json; charset=UTF-8")
	public @ResponseBody Message getUserInfo(@RequestParam(value = "userId") long userId) {
		User user = userRepository.getUserInfo(userId);
		if (user == null) {
			return MessageFactory.createErrorMessage(ErrorCode.USER_NOT_FOUND);
		}

		user.setMoney(null);
		user.setEmail(null);
		user.setLocation(null);
		return MessageFactory.createMessage(user);
	}


	@PostMapping(path = "list", produces = "application/json; charset=UTF-8")
	public @ResponseBody Message getUserList(@RequestParam(value = "users") String users) {
		return MessageFactory.createMessage(userRepository.findUserList(users));
	}
	
}

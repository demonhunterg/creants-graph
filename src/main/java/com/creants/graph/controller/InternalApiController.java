package com.creants.graph.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.exceptions.InvalidClaimException;
import com.creants.graph.dao.IUserRepository;
import com.creants.graph.om.Message;
import com.creants.graph.om.User;
import com.creants.graph.security.util.AuthHelper;
import com.creants.graph.service.MessageFactory;
import com.creants.graph.util.ErrorCode;
import com.creants.graph.util.Tracer;

/**
 * @author LamHa
 *
 */
@RestController()
@RequestMapping("/internal")
public class InternalApiController {
	@Autowired
	private IUserRepository userRepository;


	@PostMapping(path = "verify", produces = "application/json; charset=UTF-8")
	public @ResponseBody Message verify(@RequestParam(value = "key") String key,
			@RequestParam(value = "token") String token) {
		try {
			if (!isValidRequest(key))
				return MessageFactory.createErrorMessage(ErrorCode.BAD_REQUEST);

			AuthHelper.verifyToken(token);
			return MessageFactory.createMessage(userRepository.getUserInfo(AuthHelper.getUserId(token)));
		} catch (InvalidClaimException ex) {
			return MessageFactory.createErrorMessage(ErrorCode.TOKEN_EXPIRED);
		} catch (Exception e) {
			Tracer.error(this.getClass(), "verify fail! ", Tracer.getTraceMessage(e));
		}

		return MessageFactory.createErrorMessage(ErrorCode.USER_NOT_FOUND);
	}


	@PostMapping(path = "user", produces = "text/plain;charset=UTF-8")
	public @ResponseBody Message getUserInfo(@RequestHeader(value = "key") String key,
			@RequestParam(value = "id") int userId) {
		if (!isValidRequest(key))
			return MessageFactory.createErrorMessage(ErrorCode.BAD_REQUEST);

		User userInfo = userRepository.getUserInfo(userId);
		if (userInfo == null)
			return MessageFactory.createErrorMessage(ErrorCode.USER_NOT_FOUND);

		return MessageFactory.createMessage(userInfo);
	}


	// TODO move to filter
	private boolean isValidRequest(String key) {
		return "2|WqRVclir6nj4pk3PPxDCzqPTXl3J".equals(key) || "1|WqRVclir6nj4pk3PPxDCzqPTXl3J".equals(key);
	}


	@PostMapping(path = "logout", produces = "text/plain;charset=UTF-8")
	public @ResponseBody Message logout(@RequestParam(value = "token") String token,
			@RequestParam(value = "key") int key) {
		return MessageFactory.createMessage(null);
	}


	@PostMapping(path = "money", produces = "text/plain;charset=UTF-8")
	public @ResponseBody Message updateMoney(@RequestParam(value = "token") String token,
			@RequestParam(value = "value") int value) {
		try {
			AuthHelper.verifyToken(token);
			long currentMoney = userRepository.incrementUserMoney(AuthHelper.getUserId(token), value);
			Map<String, Object> data = new HashMap<>();
			data.put("money", currentMoney);
			return MessageFactory.createMessage(data);
		} catch (Exception e) {
			Tracer.error(this.getClass(), "update money fail!", Tracer.getTraceMessage(e));
		}

		return MessageFactory.createErrorMessage(ErrorCode.USER_NOT_FOUND);
	}

}

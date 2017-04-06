package com.creants.graph.controller;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.couchbase.client.java.document.json.JsonObject;
import com.creants.graph.dao.IUserRepository;
import com.creants.graph.om.Message;
import com.creants.graph.om.User;
import com.creants.graph.service.CacheService;
import com.creants.graph.service.MessageFactory;
import com.creants.graph.util.Security;

/**
 * @author LamHa
 *
 */
@RestController()
@RequestMapping("/internal")
public class InternalApiController {
	@Autowired
	private IUserRepository userRepository;

	@Autowired
	private CacheService cacheService;

	@PostMapping(path = "verify", produces = "text/plain;charset=UTF-8")
	public @ResponseBody Message verify(@RequestHeader(value = "key") String key,
			@RequestParam(value = "token") String token) {
		try {
			if (!isValidRequest(key))
				return MessageFactory.createErrorMessage(-1, "Bad Request.");

			String userInfo = cacheService.get(Security.encryptMD5(token));
			if (userInfo == null)
				return MessageFactory.createErrorMessage(1000, "User not found");

			JsonObject jo = JsonObject.fromJson(userInfo);
			return MessageFactory.createMessage(userRepository.getUserInfo(jo.getInt("id")));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return MessageFactory.createErrorMessage(1000, "User not found");
	}

	@PostMapping(path = "user", produces = "text/plain;charset=UTF-8")
	public @ResponseBody Message getUserInfo(@RequestHeader(value = "key") String key,
			@RequestParam(value = "id") int userId) {
		if (!isValidRequest(key))
			return MessageFactory.createErrorMessage(-1, "Bad Request.");

		User userInfo = userRepository.getUserInfo(userId);
		if (userInfo == null)
			return MessageFactory.createErrorMessage(1000, "User not found");

		return MessageFactory.createMessage(userInfo);
	}

	private boolean isValidRequest(String key) {
		return "2|WqRVclir6nj4pk3PPxDCzqPTXl3J".equals(key) || "1|WqRVclir6nj4pk3PPxDCzqPTXl3J".equals(key);
	}

	@RequestMapping(path = "logout", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	public @ResponseBody Message logout(@RequestParam(value = "token") String token,
			@RequestParam(value = "key") int key) {
		try {
			cacheService.delete(Security.encryptMD5(token));
			return MessageFactory.createMessage(null);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return MessageFactory.createErrorMessage(1000, "User not found");
	}

	@RequestMapping(path = "money", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	public @ResponseBody Message updateMoney(@RequestParam(value = "token") String token,
			@RequestParam(value = "value") int value) {

		try {
			String userInfo = cacheService.get(Security.encryptMD5(token));
			if (userInfo == null)
				return MessageFactory.createErrorMessage(1000, "User not found");

			// FIXME tra tao lao
			JsonObject jo = JsonObject.fromJson(userInfo);
			long currentMoney = userRepository.incrementUserMoney(jo.getInt("user_id"), value);
			Map<String, Object> data = new HashMap<>();
			data.put("money", currentMoney);
			return MessageFactory.createMessage(data);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return MessageFactory.createErrorMessage(1000, "User not found");
	}

}

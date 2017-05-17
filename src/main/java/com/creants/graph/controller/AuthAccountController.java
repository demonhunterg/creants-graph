package com.creants.graph.controller;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.creants.graph.dao.IUserRepository;
import com.creants.graph.om.Message;
import com.creants.graph.om.User;
import com.creants.graph.security.util.AuthHelper;
import com.creants.graph.service.MessageFactory;
import com.creants.graph.util.ErrorCode;
import com.creants.graph.util.Security;
import com.creants.graph.util.Tracer;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.json.JsonObject;

/**
 * @author LamHa
 *
 */
@RestController
@RequestMapping("/oauth")
public class AuthAccountController {
	private static final String FB_PROVIDER = "fb";
	volatile long atomic = 1000;
	static final AtomicLongFieldUpdater<AuthAccountController> ATOMIC_UPDATER = AtomicLongFieldUpdater
			.newUpdater(AuthAccountController.class, "atomic");

	@Autowired
	private IUserRepository userRepository;


	@PostMapping(value = "fb", produces = "application/json;charset=UTF-8")
	public @ResponseBody Message oauth(@RequestParam(value = "app_id") String appId,
			@RequestParam(value = "fb_token") String fbToken) throws Exception {

		try {
			FacebookClient facebookClient = new DefaultFacebookClient(fbToken, Version.VERSION_2_6);
			JsonObject user = facebookClient.fetchObject("me", JsonObject.class,
					Parameter.with("fields", "name,id,email,birthday"));

			long clientId = user.getLong("id");
			User userInfo = userRepository.getUserInfo(FB_PROVIDER, clientId);
			if (userInfo == null) {
				JsonObject picture = facebookClient.fetchObject("/me/picture", JsonObject.class,
						Parameter.with("type", "large"), Parameter.with("redirect", "false"));

				JsonObject data = picture.getJsonObject("data");
				userInfo = new User();
				userInfo.setAvatar(data.getString("url"));
				userInfo.setFullName(user.getString("name"));
				userInfo.setEmail(user.getString("email"));
				userRepository.insertUser(userInfo, FB_PROVIDER, clientId);
			}

			return responseMessage(userInfo, AuthHelper.createSignToken(userInfo.getUserId(), appId));
		} catch (FacebookOAuthException e) {
			return MessageFactory.createErrorMessage(ErrorCode.TOKEN_EXPIRED);
		}
	}


	@PostMapping(path = "creants", produces = "application/json;charset=UTF-8")
	public @ResponseBody Message signInWithCustom(@RequestParam(value = "username") String username,
			@RequestParam(value = "password") String password, @RequestParam(value = "app_id") String appId) {

		try {
			User user = userRepository.login(username, Security.encryptMD5(password));
			if (user == null) {
				return MessageFactory.createErrorMessage(ErrorCode.USER_NOT_FOUND);
			}

			return responseMessage(user, AuthHelper.createSignToken(user.getUserId(), appId));
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause instanceof SQLException) {
				int errorCode = ((SQLException) cause).getErrorCode();
				Tracer.error(this.getClass(), "[ERROR] signInWithCustom fail! username:" + username + ", appId: "
						+ appId + ", errorCode: " + errorCode);
				if (errorCode == 0) {
					return MessageFactory.createErrorMessage(ErrorCode.WRONG_PASSWORD);
				}

			} else {
				Tracer.error(this.getClass(),
						"[ERROR] signInWithCustom fail! username:" + username + ", appId: " + appId);
			}
		}

		return MessageFactory.createErrorMessage(ErrorCode.USER_NOT_FOUND);
	}


	/**
	 * @param deviceId
	 *            định danh duy nhất của thiết bị format: os##imei##appId (exp:
	 *            adr#3234532#1)
	 * @return
	 * 
	 */
	@PostMapping(path = "guest", produces = "application/json;charset=UTF-8")
	public @ResponseBody Message signInByGuest(@RequestParam(value = "device_id") String deviceId) {
		try {
			Tracer.debug(this.getClass(), "signInByGuest with deviceId:" + deviceId);
			User user = userRepository.loginByGuest(deviceId);
			if (user == null) {
				user = new User();
				user.setFullName("Guest#" + ATOMIC_UPDATER.getAndIncrement(this));
				user.setDeviceId(deviceId);
				userRepository.insertGuest(user);
			}

			return responseMessage(user, AuthHelper.createSignToken(user.getUserId(), "guest", deviceId));
		} catch (Exception e) {
			Tracer.error(this.getClass(), "[ERROR] signInByGuest fail! deviceId:" + deviceId,
					Tracer.getTraceMessage(e));
		}

		return MessageFactory.createErrorMessage(ErrorCode.USER_NOT_FOUND);
	}


	private Message responseMessage(User user, String token) {
		Map<String, Object> data = new HashMap<>();
		data.put("user", user);

		Message message = MessageFactory.createMessage(data);
		message.setToken(token);
		message.setPrivateKey(Security.genPrivateKey(token, user.getUserId()));
		return message;
	}

}

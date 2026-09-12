package com.creants.graph.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.creants.graph.om.Message;
import com.creants.graph.om.User;
import com.creants.graph.security.util.AuthHelper;
import com.creants.graph.service.AccountManager;
import com.creants.graph.service.MessageFactory;
import com.creants.graph.util.ErrorCode;
import com.creants.graph.util.Security;
import com.creants.graph.util.Tracer;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.json.JsonObject;

/**
 * @author LamHa
 *
 */
@RestController()
@RequestMapping("/internal")
public class InternalApiController {
	private static final String FB_PROVIDER = "fb";
	@Autowired
	private AccountManager acccountManager;


	@PostMapping(path = "verify", produces = "application/json; charset=UTF-8")
	public @ResponseBody Message verify(@RequestBody String token) {
		try {
			// verify header
//			if (!isValidRequest(key))
//				return MessageFactory.createErrorMessage(ErrorCode.BAD_REQUEST);

			token = token.replace("token=", "");
			DecodedJWT verifyToken = AuthHelper.verifyToken(token);
			String deviceId = verifyToken.getClaim("device_id").asString();
			if (deviceId != null)
				return MessageFactory.createMessage(new User(deviceId));

			User userInfo = acccountManager.getUserInfo(AuthHelper.getUserId(token));
			if (userInfo == null)
				return MessageFactory.createErrorMessage(ErrorCode.USER_NOT_FOUND);

			return MessageFactory.createMessage(userInfo);
		} catch (InvalidClaimException ex) {
			Tracer.error(this.getClass(), "verify fail! InvalidClaimException: " + token, Tracer.getTraceMessage(ex));
			return MessageFactory.createErrorMessage(ErrorCode.TOKEN_EXPIRED);
		} catch (Exception e) {
			Tracer.error(this.getClass(), "verify fail! ", Tracer.getTraceMessage(e));
		}

		return MessageFactory.createErrorMessage(ErrorCode.USER_NOT_FOUND);
	}


	@GetMapping(path = "check/uptime", produces = "application/json; charset=UTF-8")
	public @ResponseBody Message checkUpTime() {
		return MessageFactory.createMessage(System.currentTimeMillis());
	}


	@PostMapping(value = "fb", produces = "application/json;charset=UTF-8")
	public @ResponseBody Message linkFb(@RequestParam(value = "app_id") String appId,
			@RequestParam(value = "fb_token") String fbToken) throws Exception {
		try {
			Tracer.debug(this.getClass(), "fbToken:" + fbToken);
			FacebookClient facebookClient = new DefaultFacebookClient(fbToken, Version.VERSION_2_6);
			JsonObject user = facebookClient.fetchObject("me", JsonObject.class,
					Parameter.with("fields", "name,id,email,birthday"));

			String clientId = String.valueOf(user.getLong("id"));
			User userInfo = acccountManager.getUserInfo(FB_PROVIDER, clientId);
			if (userInfo != null) {
				Message existResponse = responseMessage(userInfo,
						AuthHelper.createSignToken(userInfo.getUserId(), appId));
				existResponse.setCode(ErrorCode.EXIST_USER.getId());
				return existResponse;
			}

			JsonObject picture = facebookClient.fetchObject("/me/picture", JsonObject.class,
					Parameter.with("type", "large"), Parameter.with("redirect", "false"));

			JsonObject data = picture.getJsonObject("data");
			userInfo = new User();
			userInfo.setAvatar(data.getString("url"));
			userInfo.setFullName(user.getString("name"));
			userInfo.setProvider(FB_PROVIDER);
			userInfo.setClientId(clientId);
			if (user.has("email"))
				userInfo.setEmail(user.getString("email"));
			acccountManager.insertUser(userInfo);

			return responseMessage(userInfo, AuthHelper.createSignToken(userInfo.getUserId(), appId));
		} catch (Exception e) {
			return MessageFactory.createErrorMessage(ErrorCode.UNKNOW_REASON);
		}
	}


	private Message responseMessage(User user, String token) {
		Map<String, Object> data = new HashMap<>();
		data.put("user", user);

		Message message = MessageFactory.createMessage(data);
		message.setToken(token);
		message.setPrivateKey(Security.genPrivateKey(token, user.getUserId()));
		return message;
	}


	@PostMapping(path = "user", produces = "text/plain;charset=UTF-8")
	public @ResponseBody Message getUserInfo(@RequestHeader(value = "key") String key,
			@RequestParam(value = "id") int userId) {
		if (!isValidRequest(key))
			return MessageFactory.createErrorMessage(ErrorCode.BAD_REQUEST);

		User userInfo = acccountManager.getUserInfo(userId);
		if (userInfo == null)
			return MessageFactory.createErrorMessage(ErrorCode.USER_NOT_FOUND);

		return MessageFactory.createMessage(userInfo);
	}


	// TODO move to filter
	private boolean isValidRequest(String key) {
		Tracer.debug(this.getClass(), "server_key: " +  key);
		return "2|WqRVclir6nj4pk3PPxDCzqPTXl3J".equals(key) || "1|WqRVclir6nj4pk3PPxDCzqPTXl3J".equals(key);
	}


	@PostMapping(path = "logout", produces = "text/plain;charset=UTF-8")
	public @ResponseBody Message logout(@RequestParam(value = "token") String token,
			@RequestParam(value = "key") int key) {
		return MessageFactory.createMessage(null);
	}

}

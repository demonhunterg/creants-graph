package com.creants.graph.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.creants.graph.om.Message;
import com.creants.graph.om.User;
import com.creants.graph.security.util.AuthHelper;
import com.creants.graph.service.AccountManager;
import com.creants.graph.service.MessageFactory;
import com.creants.graph.util.ErrorCode;
import com.creants.graph.util.Security;
import com.creants.graph.util.Tracer;
import com.google.api.client.auth.openidconnect.IdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.exception.FacebookException;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.json.JsonObject;

/**
 * 
 * @author LamHa
 *
 */
@RestController
@RequestMapping("/oauth")
public class OAuthController implements InitializingBean {
	private static final String FB_USER_ID = "id";
	private static final String FB_PROVIDER = "fb";
	private static final String FB_FIELDS = "name,id,email,birthday,link";
	public static final String GG_PROVIDER = "gg";

	@Override
	public void afterPropertiesSet() throws Exception {
	}

	@Autowired
	private AccountManager accountManager;

	@PostMapping(value = "fb", produces = "application/json;charset=UTF-8")
	public @ResponseBody Message oauth(@RequestParam(value = "app_id") String appId,
			@RequestParam(value = "fb_token") String fbToken) {
		Tracer.debug(this.getClass(), "1. fbToken:" + fbToken);
		try {
			FacebookClient facebookClient = new DefaultFacebookClient(fbToken, Version.VERSION_2_6);
			JsonObject user = facebookClient.fetchObject("me", JsonObject.class, Parameter.with("fields", FB_FIELDS));

			String clientId = String.valueOf(user.getLong(FB_USER_ID));
			User userInfo = accountManager.getUserInfo(FB_PROVIDER, clientId);
			if (userInfo == null) {
				JsonObject picture = facebookClient.fetchObject("/me/picture", JsonObject.class,
						Parameter.with("type", "large"), Parameter.with("redirect", "false"));

				JsonObject data = picture.getJsonObject("data");
				userInfo = new User();
				userInfo.setAvatar(data.getString("url"));
				userInfo.setFullName(user.getString("name"));
				if (user.has("email"))
					userInfo.setEmail(user.getString("email"));

				userInfo.setProvider(FB_PROVIDER);
				userInfo.setClientId(clientId);
				accountManager.insertUser(userInfo);
			}

			return responseMessage(userInfo, AuthHelper.createSignToken(userInfo.getUserId(), appId));
		} catch (FacebookOAuthException e) {
			Tracer.error(this.getClass(), Tracer.getTraceMessage(e));
			return MessageFactory.createErrorMessage(ErrorCode.TOKEN_EXPIRED);
		} catch (FacebookException e) {
			Tracer.error(this.getClass(), Tracer.getTraceMessage(e));
			return MessageFactory.createErrorMessage(ErrorCode.TOKEN_EXPIRED);
		} catch (Exception e) {
			Tracer.error(this.getClass(), Tracer.getTraceMessage(e));
			return MessageFactory.createErrorMessage(ErrorCode.UNKNOW_REASON);
		}
	}


	@PostMapping(path = "creants", produces = "application/json;charset=UTF-8")
	public @ResponseBody Message signInWithCustom(@RequestParam(value = "username") String username,
			@RequestParam(value = "password") String password, @RequestParam(value = "app_id") String appId) {

		try {
			User user = accountManager.login(username, Security.encryptMD5(password));
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


	@PostMapping(value = "gg", produces = "application/json;charset=UTF-8")
	public @ResponseBody Message oauthGG(@RequestParam(value = "app_id") String appId,
			@RequestParam(value = "gg_token") String ggToken) throws IOException {

		Tracer.debug(this.getClass(), "1. ggToken:" + ggToken);
		JacksonFactory jsonFactory = new JacksonFactory();
		NetHttpTransport transport = new NetHttpTransport();
		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
				.setAcceptableTimeSkewSeconds(864000).build();

		GoogleIdToken idToken = GoogleIdToken.parse(jsonFactory, ggToken);
		if (idToken != null && verifier.verify((IdToken) idToken)) {
			GoogleIdToken.Payload payload = idToken.getPayload();
			// Print user identifier
			String userId = payload.getSubject();
			// Get profile information from payload
			String email = payload.getEmail();
			String name = (String) payload.get("name");
			String pictureUrl = (String) payload.get("picture");

			// Use or store profile information
			// ...
			User userInfo = accountManager.getUserInfo(GG_PROVIDER, userId);
			if (userInfo == null) {
				userInfo = new User();
				userInfo.setAvatar(pictureUrl);
				userInfo.setFullName(name);
				if (email != null)
					userInfo.setEmail(email);

				userInfo.setProvider(GG_PROVIDER);
				userInfo.setClientId(userId);
				accountManager.insertUser(userInfo);
			}
			return responseMessage(AuthHelper.createSignToken(userInfo.getId(), appId));
		} else {
			System.out.println("Invalid ID token.");
			return MessageFactory.createErrorMessage(ErrorCode.TOKEN_EXPIRED);
		}
	}


	/**
	 * @param deviceId
	 *            định danh duy nhất của thiết bị format: os##imei##appId (exp:
	 *            adr#3234532#1)
	 * 
	 */
	@PostMapping(path = "guest", produces = "application/json;charset=UTF-8")
	public @ResponseBody Message signInByGuest(@RequestParam(value = "device_id") String deviceId) {
		try {
			Tracer.debug(this.getClass(), "signInByGuest with deviceId:" + deviceId);
			User user = new User(deviceId);
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
	
	private Message responseMessage(String token) {
        Message message;
        message = MessageFactory.createMessage(null);
        message.setToken(token);
        return message;
    }

}

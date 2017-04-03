package com.creants.graph.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.creants.graph.dao.IUserRepository;
import com.creants.graph.om.Message;
import com.creants.graph.om.User;
import com.creants.graph.service.MessageFactory;
import com.creants.graph.util.ErrorCode;
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
@RestController
@RequestMapping("/guest")
public class GuestController {
	@Autowired
	private IUserRepository userRepository;

	@RequestMapping(path = "link/fb", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public @ResponseBody Message linkFb(@RequestParam(value = "token") String token,
			@RequestParam(value = "fbToken") String fbToken) {
		try {
			String providerInfo = "fb";
			FacebookClient facebookClient = new DefaultFacebookClient(fbToken, Version.VERSION_2_6);
			JsonObject user = facebookClient.fetchObject("me", JsonObject.class,
					Parameter.with("fields", "name,id,email,birthday"));

			long clientId = user.getLong("id");
			User userInfo = userRepository.getUserInfo(providerInfo, clientId);
			if (userInfo == null) {
				JsonObject picture = facebookClient.fetchObject("/me/picture", JsonObject.class,
						Parameter.with("type", "large"), Parameter.with("redirect", "false"));

				com.couchbase.client.java.document.json.JsonObject jo = com.couchbase.client.java.document.json.JsonObject
						.fromJson(token);
				int userId = jo.getInt("id");

				JsonObject data = picture.getJsonObject("data");
				userInfo = new User();
				userInfo.setId(userId);
				userInfo.setAvatar(data.getString("url"));
				userInfo.setFullName(user.getString("name"));
				userInfo.setEmail(user.getString("email"));
				userRepository.linkAccountFb(userId, userInfo, clientId);
			}

			Map<String, Object> data = new HashMap<>();
			data.put("user", userInfo);

			return MessageFactory.createMessage(data);
		} catch (Exception e) {
			Tracer.error(this.getClass(), "linkFb fail! token: " + token, Tracer.getTraceMessage(e));
		}

		return MessageFactory.createErrorMessage(ErrorCode.USER_NOT_FOUND, "User not found");
	}
}

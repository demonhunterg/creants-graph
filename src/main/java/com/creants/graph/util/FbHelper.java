package com.creants.graph.util;

import com.creants.graph.om.User;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.json.JsonObject;

/**
 * @author LamHa
 *
 */
public class FbHelper {

	public static User convertToUser(String token) {
		try {
			FacebookClient facebookClient = new DefaultFacebookClient(token, Version.VERSION_2_6);
			JsonObject user = facebookClient.fetchObject("me", JsonObject.class,
					Parameter.with("fields", "name,id,email,birthday"));

			JsonObject picture = facebookClient.fetchObject("/me/picture", JsonObject.class,
					Parameter.with("type", "large"), Parameter.with("redirect", "false"));

			JsonObject data = picture.getJsonObject("data");

			User userInfo = new User();
			userInfo.setAvatar(data.getString("url"));
			userInfo.setFullName(user.getString("name"));
			userInfo.setEmail(user.getString("email"));
			return userInfo;
		} catch (Exception e) {
			Tracer.error(FbHelper.class, "getFbClient convertToUser fail!", Tracer.getTraceMessage(e));
		}

		return null;
	}

	
}

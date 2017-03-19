package com.creants.graph.controller;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.creants.graph.dao.IUserRepository;
import com.creants.graph.exception.CreantsException;
import com.creants.graph.om.Message;
import com.creants.graph.om.User;
import com.creants.graph.service.CacheService;
import com.creants.graph.service.MailService;
import com.creants.graph.service.MessageFactory;
import com.creants.graph.util.ErrorCode;
import com.creants.graph.util.IdGenerator;
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
@RequestMapping("/user")
public class AccountController {
	private static final int UID_LENGHT = 28;
	@Value("${server.domain}")
	private String hostName;

	@Value("${fb.app.id}")
	private String fbAppId;

	@Value("${fb.app.secret}")
	private String fbAppSecret;

	@Autowired
	private IUserRepository userRepository;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private MailService mailService;

	@RequestMapping(value = "/oauth", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public @ResponseBody Message oauth(@RequestParam(value = "provider") String provider,
			@RequestParam(value = "app_id") String appId, @RequestParam(value = "token") String token)
			throws Exception {
		try {
			if (!provider.equals("fb")) {
				return MessageFactory.createErrorMessage(9999, "Lỗi không xác định");
			}

			FacebookClient facebookClient = new DefaultFacebookClient(token, Version.VERSION_2_6);
			JsonObject user = facebookClient.fetchObject("me", JsonObject.class,
					Parameter.with("fields", "name,id,email,birthday"));

			long clientId = user.getLong("id");
			User userInfo = userRepository.getUserInfo(provider, clientId);
			if (userInfo == null) {
				JsonObject picture = facebookClient.fetchObject("/me/picture", JsonObject.class,
						Parameter.with("type", "large"), Parameter.with("redirect", "false"));

				JsonObject data = picture.getJsonObject("data");
				String key = IdGenerator.randomString(UID_LENGHT);
				userInfo = new User();
				userInfo.setUid(key);
				userInfo.setAvatar(data.getString("url"));
				userInfo.setFullName(user.getString("name"));
				userRepository.insertUser(userInfo, provider, clientId, user.getString("email"));
			}

			String newToken = JWT.create().withIssuer("auth0").withClaim("id", userInfo.getId())
					.sign(Algorithm.HMAC256("secret"));
			cacheService.login(newToken, newToken);

			Map<String, Object> data = new HashMap<>();
			data.put("user", userInfo);

			Message message = MessageFactory.createMessage(data);
			message.setToken(token);
			return message;

		} catch (FacebookOAuthException e) {
			return MessageFactory.createErrorMessage(1008, "Token đã hết hạn");
		}
	}

	@RequestMapping(value = "/recovery", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public @ResponseBody Message forgetPassword(@RequestParam(value = "email") String email,
			@RequestParam(value = "app_id") String appId) {
		boolean isExistEmail = userRepository.checkExistEmail(email);
		if (!isExistEmail)
			return MessageFactory.createErrorMessage(ErrorCode.USER_NOT_FOUND, "Không tồn tại email này trên hệ thống");

		String verifyCode = IdGenerator.randomString(6);
		cacheService.upsert("VERIFY_CODE_" + verifyCode, 600, email.trim());
		StringBuilder sb = new StringBuilder();
		sb.append("Dear player of Creants,");
		sb.append("\n\n");
		sb.append("We've set a temporary password so you can login to Creants and get back to the tables!");
		sb.append("\n");
		sb.append("Use this verify code to reset password: " + verifyCode);
		sb.append("\n");
		sb.append("Once you're back, you'll be able to reset your password.");
		sb.append("\n\n");
		sb.append("Best, \n The Creants Team.");

		mailService.sendMail(email, "Creants Graph Temporary Password", sb.toString());
		Map<String, Object> data = new HashMap<>();
		data.put("verify_code", verifyCode);
		data.put("ttl_second", 600);
		return MessageFactory.createMessage(data);
	}

	@RequestMapping(value = "/recovery/verify", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	public @ResponseBody Message recoveryVerify(@RequestParam(value = "verify_code") String code) {
		String email = cacheService.get("VERIFY_CODE_" + code.trim());
		if (email == null) {
			return MessageFactory.createErrorMessage(1005, "Mã xác nhận đã hết hiệu lực");
		}

		return MessageFactory.createMessage(null);
	}

	@RequestMapping(value = "/recovery/reset", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	public @ResponseBody Message resetPassword(@RequestParam(value = "verify_code") String code,
			@RequestParam(value = "password") String password) {
		String email = cacheService.get("VERIFY_CODE_" + code.trim());
		if (email == null) {
			return MessageFactory.createErrorMessage(ErrorCode.INVALID_VERIFY_CODE, "Mã xác nhận đã hết hiệu lực");
		}

		password = password.trim();
		if (password.length() < 3) {
			return MessageFactory.createErrorMessage(ErrorCode.INVALID_PASSWORD, "Password phải từ 3-18 ký tự");
		}

		int result = userRepository.updatePassword(email, password);
		if (result < 1) {
			return MessageFactory.createErrorMessage(ErrorCode.UPDATE_FAIL, "Cập nhật thất bại");
		}

		return MessageFactory.createMessage(null);
	}

	@RequestMapping(value = "/signin/fb/verify", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
	public @ResponseBody String verifyFB(@RequestParam(value = "code") String code,
			@RequestParam(value = "app_id") String appId)
			throws UnsupportedEncodingException, RestClientException, URISyntaxException {

		String redirectUrl = hostName + "/user/signin/fb/verify?app_id=" + appId;
		URI url = new URI("https://graph.facebook.com/oauth/access_token?" + "client_id=" + fbAppId + "&redirect_uri="
				+ URLEncoder.encode(redirectUrl, "UTF-8") + "&client_secret=" + fbAppId + "&code=" + code);

		String forObject = restTemplate.getForObject(url, String.class);

		String[] items = forObject.split("&");
		String accessToken = items[0].replaceFirst("access_token=", "");
		FacebookClient fbClient = new DefaultFacebookClient(accessToken, Version.VERSION_2_5);
		JsonObject fetchObject = fbClient.fetchObject("me", JsonObject.class);

		return fetchObject.toString();
	}

	@RequestMapping(path = "signin", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public @ResponseBody Message signInWithCustom(@RequestParam(value = "username") String username,
			@RequestParam(value = "password") String password, @RequestParam(value = "app_id") int appId) {
		User user = userRepository.login(username, password);
		if (user == null) {
			return MessageFactory.createErrorMessage(ErrorCode.USER_NOT_FOUND, "User not found");
		}

		try {
			String token = JWT.create().withIssuer("auth0").withClaim("id", user.getId())
					.withClaim("user_name", user.getUsername()).sign(Algorithm.HMAC256("secret"));
			cacheService.login(token, token);
			Map<String, Object> data = new HashMap<>();
			data.put("user", user);

			Message message = MessageFactory.createMessage(data);
			message.setToken(token);

			return message;
		} catch (Exception e) {
			Tracer.error(this.getClass(), "[ERROR] signInWithCustom fail! username:" + username + ", appId: " + appId,
					Tracer.getTraceMessage(e));
		}

		return MessageFactory.createErrorMessage(ErrorCode.USER_NOT_FOUND, "User not found");
	}

	@RequestMapping(path = "signout", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public @ResponseBody Message signout(@RequestParam(value = "token") String token) {
		try {
			cacheService.delete(Security.encryptMD5(token));
			return MessageFactory.createMessage(null);
		} catch (NoSuchAlgorithmException e) {
			Tracer.error(this.getClass(), "[ERROR] signout fail! token:" + token, Tracer.getTraceMessage(e));
		}

		return MessageFactory.createErrorMessage(ErrorCode.USER_NOT_FOUND, "User not found");
	}

	@RequestMapping(path = "signup", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	public @ResponseBody Message signup(@RequestParam(value = "username") String username,
			@RequestParam(value = "password") String password,
			@RequestParam(value = "email", required = false) String email, @RequestParam(value = "app_id") int appId) {

		username = username.trim();
		if (username.length() < 6) {
			return MessageFactory.createErrorMessage(ErrorCode.INVALID_USERNAME, "Tên tài khoản phải từ 6-18 ký tự");
		}

		if (password.length() < 3) {
			return MessageFactory.createErrorMessage(ErrorCode.INVALID_PASSWORD, "Password phải từ 3-18 ký tự");
		}

		if (email != null && email.length() > 0 && !isValidEmailId(email)) {
			return MessageFactory.createErrorMessage(ErrorCode.INVALID_EMAIL, "Email không hợp lệ");
		}

		String key = IdGenerator.randomString(28);
		User user = new User();
		user.setUid(key);
		user.setUsername(username);
		user.setPassword(password);
		user.setFullName(username);
		user.setEmail(email);
		try {
			userRepository.insertUser(user);
		} catch (Exception e) {
			SQLException cause = (SQLException) e.getCause();
			CreantsException creantsException = (CreantsException) cause.getCause();
			return MessageFactory.createErrorMessage(creantsException.getErrorCode() == -1 ? 1008 : 1009,
					creantsException.getMessage());
		}

		return signInWithCustom(username, password, appId);
	}

	@RequestMapping(path = "get", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
	public @ResponseBody Message getUserInfo(@RequestParam(value = "user_id") int userId) {
		User user = userRepository.getUserInfo(userId);
		if (user == null) {
			return MessageFactory.createErrorMessage(ErrorCode.USER_NOT_FOUND, "Không tìm thấy user này");
		}

		return MessageFactory.createMessage(user);
	}

	@RequestMapping(path = "update", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	public @ResponseBody Message updateUserInfo(@RequestParam(value = "token", required = true) String token,
			@RequestParam(value = "fullname", required = false) String fullName,
			@RequestParam(value = "gender", required = false) Integer gender,
			@RequestParam(value = "location", required = false) String location,
			@RequestParam(value = "birthday", required = false) String birthday) {

		int result = userRepository.updateUserInfo(16, fullName, gender, location, birthday);
		if (result != 1) {
			return MessageFactory.createErrorMessage(ErrorCode.UPDATE_FAIL, "Cập nhật thông tin thất bại");
		}

		return MessageFactory.createMessage(null);
	}

	private boolean isValidEmailId(String email) {
		String emailPattern = "\\b[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4}\\b";
		Pattern p = Pattern.compile(emailPattern);
		Matcher m = p.matcher(email);
		return m.matches();
	}

}

package com.creants.graph.controller;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.creants.graph.dao.IUserRepository;
import com.creants.graph.exception.CreantsException;
import com.creants.graph.om.Message;
import com.creants.graph.om.User;
import com.creants.graph.security.util.AuthHelper;
import com.creants.graph.service.CacheService;
import com.creants.graph.service.MessageFactory;
import com.creants.graph.util.ErrorCode;
import com.creants.graph.util.IdGenerator;
import com.creants.graph.util.Security;
import com.creants.graph.util.Tracer;

/**
 * @author LamHa
 *
 */
@RestController
@RequestMapping("/user")
public class AccountController {
	@Autowired
	private IUserRepository userRepository;
	@Autowired
	private CacheService cacheService;

	@PostMapping(path = "get", produces = "application/json; charset=UTF-8")
	public @ResponseBody Message getUserInfo(@RequestParam String token) {
		User user = userRepository.getUserInfo(AuthHelper.getUserId(token));
		if (user == null) {
			return MessageFactory.createErrorMessage(ErrorCode.USER_NOT_FOUND, "User not found");
		}

		return MessageFactory.createMessage(user);
	}

	@PostMapping(path = "update", produces = "application/json; charset=UTF-8")
	public @ResponseBody Message updateUserInfo(@RequestParam(value = "token", required = true) String token,
			@RequestParam(value = "fullname", required = false) String fullName,
			@RequestParam(value = "gender", required = false) Integer gender,
			@RequestParam(value = "location", required = false) String location,
			@RequestParam(value = "birthday", required = false) String birthday) {

		int result = userRepository.updateUserInfo(16, fullName, gender, location, birthday);
		if (result != 1) {
			return MessageFactory.createErrorMessage(ErrorCode.UPDATE_FAIL, "Update fail");
		}

		return MessageFactory.createMessage(null);
	}

	@PostMapping(path = "signout", produces = "application/json;charset=UTF-8")
	public @ResponseBody Message signout(@RequestParam(value = "token") String token) {
		try {
			cacheService.delete(Security.encryptMD5(token));
			return MessageFactory.createMessage(null);
		} catch (NoSuchAlgorithmException e) {
			Tracer.error(this.getClass(), "[ERROR] signout fail! token:" + token, Tracer.getTraceMessage(e));
		}

		return MessageFactory.createErrorMessage(ErrorCode.USER_NOT_FOUND, "User not found");
	}

	@PostMapping(path = "signup", produces = "application/json; charset=UTF-8")
	public @ResponseBody Message signup(@RequestParam(value = "username") String username,
			@RequestParam(value = "password") String password,
			@RequestParam(value = "email", required = false) String email, @RequestParam(value = "app_id") int appId) {

		username = username.trim();
		if (username.length() < 6) {
			return MessageFactory.createErrorMessage(ErrorCode.INVALID_USERNAME,
					"Invalid username. Account name must be between 6 and 18 characters");
		}

		if (password.length() < 3) {
			return MessageFactory.createErrorMessage(ErrorCode.INVALID_PASSWORD,
					"Invalid password. Password must be between 3 and 18 characters");
		}

		if (email != null && email.length() > 0 && !isValidEmailId(email)) {
			return MessageFactory.createErrorMessage(ErrorCode.INVALID_EMAIL, "Invalid email");
		}

		try {
			User user = new User();
			user.setUid(IdGenerator.generateUuid());
			user.setUsername(username);
			user.setPassword(password);
			user.setFullName(username);
			user.setEmail(email);

			userRepository.insertUser(user);
		} catch (Exception e) {
			SQLException cause = (SQLException) e.getCause();
			CreantsException creantsException = (CreantsException) cause.getCause();
			return MessageFactory.createErrorMessage(
					creantsException.getErrorCode() == -1 ? ErrorCode.TOKEN_EXPIRED : ErrorCode.UPDATE_FAIL,
					creantsException.getMessage());
		}

		// return signInWithCustom(username, password, appId);
		return null;
	}

	private boolean isValidEmailId(String email) {
		String emailPattern = "\\b[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4}\\b";
		Pattern p = Pattern.compile(emailPattern);
		Matcher m = p.matcher(email);
		return m.matches();
	}

}

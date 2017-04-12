package com.creants.graph.controller;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.creants.graph.dao.IUserRepository;
import com.creants.graph.om.Message;
import com.creants.graph.om.User;
import com.creants.graph.service.CacheService;
import com.creants.graph.service.MailService;
import com.creants.graph.service.MessageFactory;
import com.creants.graph.util.ErrorCode;
import com.creants.graph.util.IdGenerator;
import com.creants.graph.util.Security;

/**
 * @author LamHa
 *
 */
@RestController
@RequestMapping("/account")
public class AccountController {
	private static final String VERIFY_CODE_PREFIX = "VERIFY_CODE_";
	private static final int VERIFY_CODE_SECOND_TTL = 600;
	private static final int VERIFY_CODE_LENGHT = 6;

	@Autowired
	private MailService mailService;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private IUserRepository userRepository;


	@PostMapping(path = "signup", produces = "application/json; charset=UTF-8")
	public @ResponseBody Message signup(@RequestParam(value = "username") String username,
			@RequestParam(value = "password") String password,
			@RequestParam(value = "email", required = false) String email) {

		username = username.trim();
		password = password.trim();
		if (username.length() < 6) {
			return MessageFactory.createErrorMessage(ErrorCode.INVALID_USERNAME);
		}

		if (password.length() < 3) {
			return MessageFactory.createErrorMessage(ErrorCode.INVALID_PASSWORD);
		}

		if (email != null && email.length() > 0 && !isValidEmailId(email)) {
			return MessageFactory.createErrorMessage(ErrorCode.INVALID_EMAIL);
		}

		try {
			User user = new User();
			user.setUsername(username);
			user.setPassword(Security.encryptMD5(password));
			user.setFullName(username);
			user.setEmail(email);

			userRepository.insertUser(user);
		} catch (Exception e) {
			return MessageFactory.createErrorMessage(ErrorCode.EXIST_USER);
		}

		return MessageFactory.createMessage(null);
	}


	@PostMapping(value = "/recovery", produces = "application/json;charset=UTF-8")
	public @ResponseBody Message forgetPassword(@RequestParam String email,
			@RequestParam(value = "app_id") String appId) {

		boolean isExistEmail = userRepository.checkExistEmail(email);
		if (!isExistEmail)
			return MessageFactory.createErrorMessage(ErrorCode.USER_NOT_FOUND);

		String verifyCode = IdGenerator.randomString(VERIFY_CODE_LENGHT);
		cacheService.upsert(genVerifyCode(verifyCode), VERIFY_CODE_SECOND_TTL, email.trim());

		String mailTitle = "Creants Graph Temporary Password";
		mailService.sendMail(email, mailTitle, createMailContent(verifyCode));
		Map<String, Object> data = new HashMap<>();
		data.put("verify_code", verifyCode);
		data.put("ttl_second", VERIFY_CODE_SECOND_TTL);
		return MessageFactory.createMessage(data);
	}


	@PostMapping(value = "/recovery/verify", produces = "application/json; charset=UTF-8")
	public @ResponseBody Message recoveryVerify(@RequestParam(value = "verify_code") String code) {
		String email = cacheService.get(VERIFY_CODE_PREFIX + code.trim());
		if (email == null) {
			return MessageFactory.createErrorMessage(ErrorCode.VERIFY_CODE_EXPIRED);
		}

		return MessageFactory.createMessage(null);
	}


	@PostMapping(value = "/recovery/reset", produces = "application/json; charset=UTF-8")
	public @ResponseBody Message resetPassword(@RequestParam(value = "verify_code") String code,
			@RequestParam String password) {

		String email = cacheService.get(genVerifyCode(code));
		if (email == null) {
			return MessageFactory.createErrorMessage(ErrorCode.INVALID_VERIFY_CODE);
		}

		password = password.trim();
		if (password.length() < 3) {
			return MessageFactory.createErrorMessage(ErrorCode.INVALID_PASSWORD);
		}

		try {
			int result = userRepository.updatePassword(email, Security.encryptMD5(password));
			if (result < 1) {
				return MessageFactory.createErrorMessage(ErrorCode.UPDATE_FAIL);
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return MessageFactory.createMessage(null);
	}


	private String genVerifyCode(String code) {
		return VERIFY_CODE_PREFIX + code.trim();
	}


	private String createMailContent(String verifyCode) {
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
		return sb.toString();
	}


	private boolean isValidEmailId(String email) {
		String emailPattern = "\\b[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4}\\b";
		Pattern p = Pattern.compile(emailPattern);
		Matcher m = p.matcher(email);
		return m.matches();
	}
}

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
import com.creants.graph.service.CacheService;
import com.creants.graph.service.MailService;
import com.creants.graph.service.MessageFactory;
import com.creants.graph.util.ErrorCode;
import com.creants.graph.util.IdGenerator;

/**
 * @author LamHa
 *
 */
@RestController
@RequestMapping("/user")
public class RecoveryAccountController {
	private static final int VERIFY_CODE_SECOND_TTL = 600;
	private static final int VERIFY_CODE_LENGHT = 6;

	@Autowired
	private MailService mailService;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private IUserRepository userRepository;

	@RequestMapping(value = "/recovery", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public @ResponseBody Message forgetPassword(@RequestParam(value = "email") String email,
			@RequestParam(value = "app_id") String appId) {

		boolean isExistEmail = userRepository.checkExistEmail(email);
		if (!isExistEmail)
			return MessageFactory.createErrorMessage(ErrorCode.USER_NOT_FOUND, "User not found");

		String verifyCode = IdGenerator.randomString(VERIFY_CODE_LENGHT);
		cacheService.upsert(genVerifyCode(verifyCode), VERIFY_CODE_SECOND_TTL, email.trim());

		String mailTitle = "Creants Graph Temporary Password";
		mailService.sendMail(email, mailTitle, createMailContent(verifyCode));
		Map<String, Object> data = new HashMap<>();
		data.put("verify_code", verifyCode);
		data.put("ttl_second", VERIFY_CODE_SECOND_TTL);
		return MessageFactory.createMessage(data);
	}

	@RequestMapping(value = "/recovery/verify", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	public @ResponseBody Message recoveryVerify(@RequestParam(value = "verify_code") String code) {
		String email = cacheService.get("VERIFY_CODE_" + code.trim());

		if (email == null) {
			return MessageFactory.createErrorMessage(ErrorCode.VERIFY_CODE_EXPIRED, "Verify code expired");
		}

		return MessageFactory.createMessage(null);
	}

	@RequestMapping(value = "/recovery/reset", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	public @ResponseBody Message resetPassword(@RequestParam(value = "verify_code") String code,
			@RequestParam(value = "password") String password) {

		String email = cacheService.get(genVerifyCode(code));
		if (email == null) {
			return MessageFactory.createErrorMessage(ErrorCode.INVALID_VERIFY_CODE, "Invalid verify code");
		}

		password = password.trim();
		if (password.length() < 3) {
			return MessageFactory.createErrorMessage(ErrorCode.INVALID_PASSWORD,
					"Invalid password. Password must be between 3 and 18 characters");
		}

		int result = userRepository.updatePassword(email, password);
		if (result < 1) {
			return MessageFactory.createErrorMessage(ErrorCode.UPDATE_FAIL, "Update fail");
		}

		return MessageFactory.createMessage(null);
	}

	private String genVerifyCode(String code) {
		return "VERIFY_CODE_" + code.trim();
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

}

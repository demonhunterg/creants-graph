package com.creants.graph.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * @author LamHa
 * Để cho phép mình gửi mail đi thì cần bật cho phép truy cập gửi từ app
 * https://www.google.com/settings/security/lesssecureapps
 *
 */
@Service
public class MailService {
	private JavaMailSender mailSender;

	@Autowired
	public MailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	/**
	 * This method will send compose and send the message
	 */
	public void sendMail(String to, String subject, String body) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject(subject);
		message.setText(body);
		mailSender.send(message);
	}

}

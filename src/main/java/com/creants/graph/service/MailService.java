package com.creants.graph.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.creants.graph.CreantsGraphApplication;
import com.creants.graph.util.Tracer;

/**
 * @author LamHa Để cho phép mình gửi mail đi thì cần bật cho phép truy cập gửi
 *         từ app https://www.google.com/settings/security/lesssecureapps
 *
 */
@Service
public class MailService {
	private JavaMailSender mailSender;

	private @Value("${spring.mail.username}") String from;

	@Autowired
	public MailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	/**
	 * This method will send compose and send the message
	 */
	public void sendMail(String to, String subject, String body) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(from);
			message.setTo(to);
			message.setSubject(subject);
			message.setText(body);
			mailSender.send(message);
		} catch (Exception e) {
			Tracer.error(CreantsGraphApplication.class, "ERROR", Tracer.getTraceMessage(e));
		}
	}
	

}

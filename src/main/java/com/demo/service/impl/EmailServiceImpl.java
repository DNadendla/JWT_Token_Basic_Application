package com.demo.service.impl;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.demo.service.EmailService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

	@Autowired
	private JavaMailSender javaMailSender;

	@Override
	public void testMail() {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo("dattu1n2@gmail.com");
			// message.setFrom("ttttt@@gmail.com");
			message.setSubject("Test Mail");
			message.setText("--------------Email Testing from App--------------");
			javaMailSender.send(message);
			System.err.println("Email Sent");
		} catch (MailException e) {
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	@Override
	public void sendNewPasswordEmail(String firstName, String password, String email) {
		String text = "Hello " + firstName + ",<br> <br> Your new accounr password is: <strong style= 'color: red;'>" + password
				+ "</strong> <br> <br> Support Team";
		try {
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
			helper.setText(text, true);
			helper.setTo(email);
			helper.setFrom("noreply-email@test.com");
			helper.setSubject("Email Test - Password");
			javaMailSender.send(mimeMessage);
		} catch (MailException e) {
			log.error(e.getMessage());
		} catch (MessagingException e) {
			log.error(e.getMessage());
		}
	}
}

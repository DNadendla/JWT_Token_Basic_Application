package com.demo.service;

public interface EmailService {

	void testMail();

	void sendNewPasswordEmail(String firstName, String password, String email);

}

package com.demo.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import com.demo.service.LoginAttemptService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AuthenticationFailureListener {

	@Autowired
	private LoginAttemptService loginAttemptService;
	
	//Fires whenever there is a Invalid Authentication/Authentication failure happens
	@EventListener 
	private void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent failureEvent) {
		log.info("AuthenticationFailureListener :: onAuthenticationFailure");
	
		Object principle = failureEvent.getAuthentication().getPrincipal();	
		if (principle instanceof String) {
			String userName = (String) principle;
			loginAttemptService.addUserToLoginAttemptCahce(userName);
		}
	}
}

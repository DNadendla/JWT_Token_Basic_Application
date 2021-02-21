package com.demo.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.demo.domain.UserPrincipal;
import com.demo.service.LoginAttemptService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AuthenticationSuccessListener {

	@Autowired
	private LoginAttemptService loginAttemptService;
	
	//Fires whenever there is a valid Authentication i.e username & password are valid
	@EventListener
	public void onAuthenticationSuccess(AuthenticationSuccessEvent successEvent) {
		log.info("AuthenticationSuccessListener :: onAuthenticationSuccess");
		Object principal = successEvent.getAuthentication().getPrincipal();
		if(principal instanceof UserDetails) {
			UserPrincipal userPrincipal = (UserPrincipal) principal;
			loginAttemptService.evictUserFromLoginAttemptCache(userPrincipal.getUsername());
		}
	}
	
}

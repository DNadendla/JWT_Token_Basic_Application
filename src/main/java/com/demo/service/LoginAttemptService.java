package com.demo.service;

public interface LoginAttemptService {

	void addUserToLoginAttemptCahce(String userName);

	boolean hasExceededMaxAttempts(String userName);

	void evictUserFromLoginAttemptCache(String userName);

}

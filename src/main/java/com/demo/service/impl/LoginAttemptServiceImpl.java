package com.demo.service.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.demo.service.LoginAttemptService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoginAttemptServiceImpl implements LoginAttemptService { 
	private static final  Integer MAX_NUMBER_OF_ATTEMPTS = 5;
	private static final  Integer ATTEMPT_INCREMENT = 1; 
	private LoadingCache<String, Integer> loginAttemptCache;
	 
	public LoginAttemptServiceImpl() {
		super();
		//Initializing the Cache
		loginAttemptCache = CacheBuilder.newBuilder()
										.expireAfterWrite(10, TimeUnit.MINUTES)  // cache will expire after 10 minutes of access
										.maximumSize(100) // maximum 100 records can be cached
										.build(new CacheLoader<String, Integer>() {  // build the cache loader

											@Override
											public Integer load(String key) throws Exception {
												// TODO Auto-generated method stub
												return 0;
											}
										}); 
	}
	
	//Adds the User to Cache
	@Override
	public void addUserToLoginAttemptCahce(String userName) {
		int attempts = 0;
		try {
			Integer invalidLoginAttemps = loginAttemptCache.get(userName);
			attempts = ATTEMPT_INCREMENT + invalidLoginAttemps;
			loginAttemptCache.put(userName, attempts);
		} catch (ExecutionException e) {
			log.error(e.getMessage());
		}
	}
	
	//Removes the user from cache
	@Override
	public void evictUserFromLoginAttemptCache(String userName) {
		loginAttemptCache.invalidate(userName);
	} 
	
	//Checks the User exceeds invalid attempt limit count
	@Override
	public boolean hasExceededMaxAttempts(String userName) {
		try {
			Integer invalidLoginAttemps = loginAttemptCache.get(userName);
			return  invalidLoginAttemps >= MAX_NUMBER_OF_ATTEMPTS;
		} catch (ExecutionException e) {
			log.error(e.getMessage());
		}
		return false;
	}
}

package com.demo.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.constant.SecurityConstant;
import com.demo.domain.User;
import com.demo.domain.UserPrincipal;
import com.demo.exception.domain.CustomExceptionHandling;
import com.demo.exception.domain.EmailExistsException;
import com.demo.exception.domain.UserNotFoundException;
import com.demo.exception.domain.UsernameExistsException;
import com.demo.service.UserService;
import com.demo.utility.JwtTokenProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/user")
public class UserResource extends CustomExceptionHandling {

	@Autowired
	private UserService userService;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private JwtTokenProvider jwtTokenProvider;
	
	@PostMapping("/register")
	public ResponseEntity<User> register(@RequestBody User user) throws UserNotFoundException, EmailExistsException, UsernameExistsException {
		User userRegistered = userService.register(user.getFirstName(), user.getLastName(), user.getUserName(), user.getEmail());
		return new ResponseEntity<User>(userRegistered, HttpStatus.OK);
	}
	
	@PostMapping("/login")
	public ResponseEntity<User> login(@RequestBody User user) {
		authenticateUser(user.getUserName(), user.getPassword());
		User loginUser = userService.findUserByUserName(user.getUserName());
		UserPrincipal userPrincipal = new UserPrincipal(loginUser);
		HttpHeaders httpHeaders = getJWTHeaders(userPrincipal);
		return new ResponseEntity<User>(loginUser, httpHeaders, HttpStatus.OK);
	}
	
	
	
	
	private HttpHeaders getJWTHeaders(UserPrincipal userPrincipal) {
		String jwtToken = jwtTokenProvider.generateJwtToken(userPrincipal);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(SecurityConstant.JWT_TOKEN_HEADER, jwtToken);
		return httpHeaders;
	}


	private void authenticateUser(String userName, String password) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
	}


	// If any exception occurs in this method flow, then it looks for exception
	// handling code in the above extending CustomExceptionHandling class
	@GetMapping
	public String getMessage() {
		log.info("UserResource :: getMessage");
		return "Test home";
	}

	@PostMapping("/home")
	public String testException() throws HttpRequestMethodNotSupportedException {
		//throw new EmailExistsException("Email already taken!....");
		//throw new HttpRequestMethodNotSupportedException("Method not supported!....");
		return "Hello";
	}
}

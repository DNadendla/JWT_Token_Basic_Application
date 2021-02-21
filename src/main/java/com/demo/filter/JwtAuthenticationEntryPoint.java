package com.demo.filter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Component;

import com.demo.constant.SecurityConstant;
import com.demo.domain.CustomHttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint extends Http403ForbiddenEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authenticationException) throws IOException {
		log.info("Inside JwtAuthenticationEntryPoint :: commence() ==> Login failed/invalid authentication");

		CustomHttpResponse customHttpResponse = new CustomHttpResponse(new Date(), HttpStatus.FORBIDDEN.value(),
				HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.getReasonPhrase(), SecurityConstant.FORBIDDEN_MESSAGE);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE); //setting content type as application/json
		response.setStatus(HttpStatus.FORBIDDEN.value());
		
		OutputStream outputStream = response.getOutputStream();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(outputStream, customHttpResponse);
		outputStream.flush();
		
	}
}

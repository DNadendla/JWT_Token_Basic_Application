package com.demo.exception.domain;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.domain.CustomHttpResponse;

@RestController
public class GlobalErrorHandler extends CustomExceptionHandling implements ErrorController {

	public static final String ERROR_PATH = "/error";

	@RequestMapping(ERROR_PATH)
	public ResponseEntity<CustomHttpResponse> notFound404() {
		return createHttpResponse(NOT_FOUND, "There is no mapping for this URL");
	}

	@Override
	public String getErrorPath() {
		return ERROR_PATH;
	}
}

package com.demo.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.demo.constant.SecurityConstant;
import com.demo.utility.JwtTokenProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		log.info("JwtAuthorizationFilter :: doFilterInternal");
		
		// To Make sure user is valid and request is valid so we can set the user as a Authenticated user
		
		if (request.getMethod().equals(SecurityConstant.OPTIONS_HTTP_METHOD)) {
			// Options request is send before every request to make sure/gather info
			// regarding server
			response.setStatus(HttpStatus.OK.value());
		} else {
			String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
			if (authorizationHeader == null || !authorizationHeader.startsWith(SecurityConstant.TOKEN_PREFIX)) {
				filterChain.doFilter(request, response);
				return;
			}
			
			String token = authorizationHeader.substring(SecurityConstant.TOKEN_PREFIX.length()); // Removes "Bearer " and gives us the token
			String userName = jwtTokenProvider.getSubject(token);
			if(jwtTokenProvider.isTokenValid(userName, token) && SecurityContextHolder.getContext() == null) {
				List<GrantedAuthority> authorities = jwtTokenProvider.getAuthoritiesFromToken(token);
				Authentication authentication = jwtTokenProvider.getAuthentication(userName, authorities, request);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} else { //If the token is failed i.e token is expired then clear the context
				SecurityContextHolder.clearContext();
			}
			
			filterChain.doFilter(request, response);
		}

	}

}

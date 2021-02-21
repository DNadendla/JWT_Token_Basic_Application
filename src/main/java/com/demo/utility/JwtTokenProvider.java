package com.demo.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.demo.constant.SecurityConstant;
import com.demo.domain.UserPrincipal;

@Component
public class JwtTokenProvider {

	@Value("{jwt.secret}")
	private String secret;

	
	//Token Generation
	public String generateJwtToken(UserPrincipal userPrincipal) {
		String[] claims = getAuthoritiesFromUser(userPrincipal);
		return JWT.create()
				.withIssuer(SecurityConstant.GET_ARRAYS_LLC)
				.withAudience(SecurityConstant.GET_ARRAYS_ADMINISTRATION)
				.withIssuedAt(new Date())
				.withSubject(userPrincipal.getUsername()) 	 //Unique identifier for  the user;
				.withArrayClaim(SecurityConstant.AUTHORITIES, claims)
				.withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstant.EXPIRATION_TIME))
				.sign(Algorithm.HMAC512(secret.getBytes()));
	}

	private String[] getAuthoritiesFromUser(UserPrincipal userPrincipal) {
		List<String> authorities = new ArrayList<>();
		for (GrantedAuthority grantedAuthority : userPrincipal.getAuthorities()) {
			authorities.add(grantedAuthority.getAuthority());
		}
		return authorities.toArray(new String[0]);
		
		/* Functional Style - Java 8
		 * 
		 * return userPrincipal.getAuthorities()
                 .stream()
                 .map(GrantedAuthority::getAuthority)
                 .toArray(String[]::new);
         */
	}

	// Read info from Token
	public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
		String[] claims = getClaimsFromToken(token); // claims = authoirities
		return Arrays.stream(claims).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
	}

	private String[] getClaimsFromToken(String token) {
		JWTVerifier verifier = getJWTVerifier();
		Claim claim = verifier.verify(token).getClaim(SecurityConstant.AUTHORITIES);
		return claim.asArray(String.class);
	}

	private JWTVerifier getJWTVerifier() {
		JWTVerifier jwtVerifier;
		try {
			Algorithm algorithm = Algorithm.HMAC512(secret);
			jwtVerifier = JWT.require(algorithm).withIssuer(SecurityConstant.GET_ARRAYS_LLC).build();
		} catch (JWTVerificationException verificationException) {
			throw new JWTVerificationException(SecurityConstant.JWT_TOKEN_CANT_BE_VERIFIED);
		}
		return jwtVerifier;
	}

	// Authentication
	public Authentication getAuthentication(String userName, List<GrantedAuthority> authorities,
			HttpServletRequest httpServletRequest) {
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
				userName, null, authorities); // No need to pass credentials/password because token is already verfied
		usernamePasswordAuthenticationToken
				.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest)); // Check without
																									// this line and
																									// verify in Udemy
		return usernamePasswordAuthenticationToken;
	}

	// Token Validations
	public boolean isTokenValid(String userName, String token) {
		JWTVerifier jwtVerifier = getJWTVerifier();
		boolean isTokenValid = StringUtils.isNotEmpty(token) && !isTokenExpired(jwtVerifier, token);
		return isTokenValid;
	}

	private boolean isTokenExpired(JWTVerifier jwtVerifier, String token) {
		Date expiration = jwtVerifier.verify(token).getExpiresAt();
		boolean isTokenExpired = expiration.before(new Date());
		return isTokenExpired;
	}
	
	// Get Subject
	public String getSubject(String token) {
		JWTVerifier jwtVerifier = getJWTVerifier();
		String subject = jwtVerifier.verify(token).getSubject();
		return subject;
	}
	
	
	
}

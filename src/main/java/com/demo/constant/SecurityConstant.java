package com.demo.constant;

public class SecurityConstant {
	
	public static final long EXPIRATION_TIME 				= 432_000_000; //5 days expressed in milliseconds 

	public static final String TOKEN_PREFIX  				= "Bearer "; // "bearer" grants you access to something
	
	public static final String JWT_TOKEN_HEADER 			= "Jwt-Token"; //Custom Header or name that represents Token
	
	public static final String JWT_TOKEN_CANT_BE_VERIFIED   = "Token cannot be verified"; //Custom Header
	
	public static final String GET_ARRAYS_LLC 				="Get Arrays, LLC"; //Token Provider name
	
	public static final String GET_ARRAYS_ADMINISTRATION 	= "User Management Portal";
	
	public static final String AUTHORITIES 					= "authorities"; //User's roles
	
	public static final String  FORBIDDEN_MESSAGE			= "You need to login to access this page";
	
	public static final String ACCESS_DENIED_MESSAGE	 	= "You do not have permission to access this page";	

	public static final String OPTIONS_HTTP_METHOD 			= "OPTIONS"; //To verify whether the server accepts the request or not
	
	public static final String[] PUBLIC_URLS 				= { "/user/login" ,"/user/register"
																,"/user/resetpassword/**" ,"/user/image/**" };
	 
	//public static final String[] PUBLIC_URLS 				= { "**" }; //All Requests are allowed
	 
}

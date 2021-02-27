package com.demo.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.demo.constant.SecurityConstant;
import com.demo.filter.JwtAccessDeniedHandler;
import com.demo.filter.JwtAuthenticationEntryPoint;
import com.demo.filter.JwtAuthorizationFilter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private JwtAuthorizationFilter jwtAuthorizationFilter;
	
	@Autowired
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	
	@Autowired
	private JwtAccessDeniedHandler jwtAccessDeniedHandler;
	
	@Autowired
	@Qualifier("userDetailsService")
	private UserDetailsService userDetailsService; 
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		log.info("SecurityConfiguration :: configure(AuthenticationManagerBuilder auth) ==> Configuring User Details Service");
		auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		log.info("SecurityConfiguration :: configure(HttpSecurity http) ==> Configuring urls, filters, etc relateed to Security");
		http.csrf().disable() //Disables Cross Site Request Forgery
		.cors() // Adding Cross origin resource sharing  
		.and()
		.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		.and()
		.authorizeRequests().antMatchers(SecurityConstant.PUBLIC_URLS).permitAll()
		.anyRequest().authenticated()
		.and()
		.exceptionHandling().accessDeniedHandler(jwtAccessDeniedHandler)
		.authenticationEntryPoint(jwtAuthenticationEntryPoint)
		.and()
		.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
	}
	
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		log.info("authenticationManagerBean initialization");
		return super.authenticationManagerBean(); 
	}
	
}

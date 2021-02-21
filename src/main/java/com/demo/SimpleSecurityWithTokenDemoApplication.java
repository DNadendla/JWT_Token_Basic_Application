package com.demo;

import java.io.File;

import org.aspectj.apache.bcel.classfile.Field;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.demo.constant.FileConstant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class SimpleSecurityWithTokenDemoApplication {

	/*
	 * @Autowired private EmailService emailService;
	 */
	public static void main(String[] args) {
		log.info("SimpleSecurityWithTokenDemoApplication :: main");
		SpringApplication.run(SimpleSecurityWithTokenDemoApplication.class, args);
		log.info("Home Path: "+System.getProperty("user.home"));
		new File(FileConstant.USER_FOLDER).mkdirs();
	}

	/*
	 * @EventListener(ApplicationReadyEvent.class) public void triggerMail() {
	 * emailService.testMail(); }
	 */

	@Bean
	public BCryptPasswordEncoder bCPasswordEncoder() {
		log.info("SimpleSecurityWithTokenDemoApplication :: bCPasswordEncoder generating Password Encoder Object");
		return new BCryptPasswordEncoder();
	}

}

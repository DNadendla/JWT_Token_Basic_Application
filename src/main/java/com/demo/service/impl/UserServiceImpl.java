package com.demo.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.demo.constant.FileConstant;
import com.demo.domain.User;
import com.demo.domain.UserPrincipal;
import com.demo.enumeration.Role;
import com.demo.exception.domain.EmailExistsException;
import com.demo.exception.domain.EmailNotFoundException;
import com.demo.exception.domain.UserNotFoundException;
import com.demo.exception.domain.UsernameExistsException;
import com.demo.repository.UserRepository;
import com.demo.service.EmailService;
import com.demo.service.LoginAttemptService;
import com.demo.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@Qualifier("userDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private LoginAttemptService loginAttemptService;
	
	@Autowired
	private EmailService emailService;

	// Method called by Spring security to load the user
	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		log.info("UserServiceImpl :: loadUserByUsername " + userName);
		User user = userRepository.findUserByUserName(userName);
		if (user == null) {
			log.error("No User Found by username: " + userName);
			 throw new UsernameNotFoundException("User not found by username" + userName);
		} else {
			validateLoginAttempt(user);
			user.setLastLoginDateDisplay(user.getLastLoginDate());
			user.setLastLoginDate(new Date());
			user = userRepository.save(user);
			UserPrincipal userPrincipal = new UserPrincipal(user);
			log.info("Returning found user by username: "+userName);
			return userPrincipal;
		}
	}

	private void validateLoginAttempt(User user) {
		if (user.isNotLocked()) {
			boolean hasExceededMaxAttempts = loginAttemptService.hasExceededMaxAttempts(user.getUserName());
			if(hasExceededMaxAttempts) {
				user.setNotLocked(false);
			} else {
				user.setNotLocked(true);
			}
		} else {
			loginAttemptService.evictUserFromLoginAttemptCache(user.getUserName());
		}
	}

	@Override
	public User register(String firstName, String lastName, String userName, String email)
			throws UserNotFoundException, EmailExistsException, UsernameExistsException {
		validateNewUserNameAndEmail("", userName, email);

		User user = new User();
		user.setUserId(generateUserId());
		String password = generatePassword();
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setUserName(userName);
		user.setEmail(email);
		user.setJoinDate(new Date());
		user.setPassword(encodePassword(password));
		user.setActive(true);
		user.setNotLocked(true);
		user.setRole(Role.ROLE_USER.name());
		user.setAuthorities(Role.ROLE_USER.getAuthorities());
		user.setProfileImageUrl(getTemporaryProfileImageUrl(userName));
		userRepository.save(user);
		log.info("New user password: " + password);
		
		emailService.sendNewPasswordEmail(firstName, password, email);
		
		loginAttemptService.evictUserFromLoginAttemptCache(user.getUserName());
		return user;
	}

	private String encodePassword(String password) {
		String encodedPassword = bCryptPasswordEncoder.encode(password);
		return encodedPassword;
	}

	private String generatePassword() {
		String randomPassword = RandomStringUtils.randomAlphanumeric(10);
		return randomPassword;
	}

	private String generateUserId() {
		String randomUserId =  RandomStringUtils.randomNumeric(10);
		return randomUserId;
	}

	private User validateNewUserNameAndEmail(String currentUserName, String newUserName, String newEmail) throws UserNotFoundException, EmailExistsException, UsernameExistsException {
		User userByNewUserName = findUserByUserName(newUserName);
		User userByNewEmail = findUserByEmail(newEmail);
		//Validate Available User
		if (StringUtils.isNotBlank(currentUserName)) {
			User currentUser = findUserByUserName(currentUserName);
			if (currentUser == null) {
				throw new UserNotFoundException("No User found by UserName: "+currentUserName);
			}
			
			if(userByNewUserName != null && !currentUser.getId().equals(userByNewUserName.getId())) {
				throw new UsernameExistsException("UserName exists: "+newUserName);
			}
			
			if(userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
				throw new EmailExistsException("Email Exists: "+newEmail);
			}
			
			return currentUser;
		} else {
			if(userByNewUserName != null) {
                throw new UsernameExistsException("UserName exists: "+newUserName);
            }
            if(userByNewEmail != null) {
                throw new EmailExistsException("Email Exists: "+newEmail);
            }
            
			return null;
		}
		
		//Validate New User
	}

	@Override
	public List<User> getUsers() {
		return userRepository.findAll();
	}

	@Override
	public User findUserByUserName(String userName) {
		return userRepository.findUserByUserName(userName);
	}

	@Override
	public User findUserByEmail(String email) {
		return userRepository.findUserByEmail(email);
	}

	@Override
	public User addNewUser(String firstName, String lastName, String username, String email, String role,
			boolean isNonLocked, boolean isActive, MultipartFile profileImage)
			throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException {
		validateNewUserNameAndEmail("", username, email);
		User user = new User();
        String password = generatePassword();
        user.setUserId(generateUserId());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setJoinDate(new Date());
        user.setUserName(username);
        user.setEmail(email);
        user.setPassword(encodePassword(password));
        user.setActive(isActive);
        user.setNotLocked(isNonLocked);
        user.setRole(getRoleEnumName(role).name());
        user.setAuthorities(getRoleEnumName(role).getAuthorities());
        user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
        userRepository.save(user);
        //
        saveProfileImage(user, profileImage);
        log.info("New user password: " + password);
        return user;
	}
	
	@Override
	public User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername,
			String newEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage)
			throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException {
		User currentUser = validateNewUserNameAndEmail(currentUsername, newUsername, newEmail);
		currentUser.setFirstName(newFirstName);
		currentUser.setLastName(newLastName);
		currentUser.setUserName(newUsername);
		currentUser.setEmail(newEmail);
		currentUser.setActive(isActive);
		currentUser.setNotLocked(isNonLocked);
		currentUser.setRole(getRoleEnumName(role).name());
		currentUser.setAuthorities(getRoleEnumName(role).getAuthorities());
        userRepository.save(currentUser);
        //
        saveProfileImage(currentUser, profileImage);
        return currentUser;
	}


	@Override
	public void deleteUser(Long id) {
		userRepository.deleteById(id);
	}

	@Override
	public void resetPassword(String email) throws EmailNotFoundException {
		User user = userRepository.findUserByEmail(email);
		if (user == null) {
			throw new EmailNotFoundException("User not available by email: "+email);
		}
		String password = generatePassword();
		user.setPassword(encodePassword(password));
		userRepository.save(user);
		log.info("New Password: "+ password);
		emailService.sendNewPasswordEmail(user.getFirstName(), password, user.getEmail());
	}

	@Override
	public User updateProfileImage(String username, MultipartFile profileImage) throws 
									UserNotFoundException, EmailExistsException, UsernameExistsException, IOException {
		User user = validateNewUserNameAndEmail(username, null, null);
		saveProfileImage(user, profileImage);
		return user;
	}

	
	private String getTemporaryProfileImageUrl(String userName) {
		String profileImageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
															.path(FileConstant.DEFAULT_USER_IMAGE_PATH + userName).toUriString();
		return profileImageUrl;
	}

	private void saveProfileImage(User user, MultipartFile profileImage) throws IOException {
		if (profileImage != null) {
			//Check  user-folder available or not
			Path userFolder = Paths.get(FileConstant.USER_FOLDER+user.getUserName()).toAbsolutePath().normalize();
			if(!Files.exists(userFolder)) {
				Files.createDirectories(userFolder);	
				log.info("Directory Created: " + userFolder);
			}
			
			//Remove the existing profile picture
			Files.deleteIfExists(Paths.get(userFolder + user.getUserName() + ".jpg"));
			
			//Put the new Image 
			/**
			 * StandardCopyOption.REPLACE_EXISTING -> Mentioning to Replace the existing image with new Image
			 * Above delete option also deletes the file but this is like a double check 
			 */
			Files.copy(profileImage.getInputStream(), userFolder.resolve(user.getUserName() + ".jpg"), StandardCopyOption.REPLACE_EXISTING);
			user.setProfileImageUrl(setProfileImageUrl(user.getUserName()));
			userRepository.save(user);
			
			log.info("File Saved in System: "+profileImage.getOriginalFilename());
		}
		
	}

	private String setProfileImageUrl(String userName) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(FileConstant.USER_IMAGE_PATH+userName+".jpg").toUriString();
	}

	private Role getRoleEnumName(String role) {
		return Role.valueOf(role.toUpperCase());
	}
}

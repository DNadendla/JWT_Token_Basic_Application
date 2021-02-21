package com.demo.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.demo.domain.User;
import com.demo.exception.domain.EmailExistsException;
import com.demo.exception.domain.EmailNotFoundException;
import com.demo.exception.domain.UserNotFoundException;
import com.demo.exception.domain.UsernameExistsException;

public interface UserService {

	public User register(String firstName, String lastName, String userName, String email) throws UserNotFoundException, EmailExistsException, UsernameExistsException;
	
	public List<User> getUsers();
	
	User findUserByUserName(String userName);
	
	User findUserByEmail(String email);
	
    User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException;

    User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException;

    void deleteUser(Long id);

    void resetPassword(String email) throws EmailNotFoundException;

    User updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, EmailExistsException, UsernameExistsException, IOException;
	
}

package com.demo.resource;

import static org.springframework.http.HttpStatus.OK;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.demo.constant.FileConstant;
import com.demo.constant.SecurityConstant;
import com.demo.domain.CustomHttpResponse;
import com.demo.domain.User;
import com.demo.domain.UserPrincipal;
import com.demo.exception.domain.CustomExceptionHandling;
import com.demo.exception.domain.EmailExistsException;
import com.demo.exception.domain.EmailNotFoundException;
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
	
	
	@PostMapping("/add")
	public ResponseEntity<User> addNewUser(@RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("role") String role,
            @RequestParam("isActive") String isActive,
            @RequestParam("isNonLocked") String isNonLocked,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) 
            		throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException {
		 User newUser = userService.addNewUser(firstName, lastName, username,email, role, Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
	        return new ResponseEntity<>(newUser, OK);
	}
	
	@PostMapping("/update")
    public ResponseEntity<User> update(@RequestParam("currentUsername") String currentUsername,
                                       @RequestParam("firstName") String firstName,
                                       @RequestParam("lastName") String lastName,
                                       @RequestParam("username") String username,
                                       @RequestParam("email") String email,
                                       @RequestParam("role") String role,
                                       @RequestParam("isActive") String isActive,
                                       @RequestParam("isNonLocked") String isNonLocked,
                                       @RequestParam(value = "profileImage", required = false) MultipartFile profileImage)
                                    		   throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException {
        User updatedUser = userService.updateUser(currentUsername, firstName, lastName, username,email, role, Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
        return new ResponseEntity<>(updatedUser, OK);
    }
	
    @GetMapping("/find/{username}") //Path Parameters or URL Parameters
    public ResponseEntity<User> getUser(@PathVariable("username") String username) {
        User user = userService.findUserByUserName(username);
        return new ResponseEntity<>(user, OK);
    }
    
    @GetMapping("/list")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getUsers();
        return new ResponseEntity<>(users, OK);
    }
    
    @GetMapping("/resetpassword/{email}")
    public ResponseEntity<CustomHttpResponse> resetPassword(@PathVariable("email") String email) throws MessagingException, EmailNotFoundException {
        userService.resetPassword(email);
        return prepareResponse(OK, "An email with new password was sent to " + email);
    }
	
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('user:delete')")
    public ResponseEntity<CustomHttpResponse> deleteUser(@PathVariable("id") Long id) throws IOException {
        userService.deleteUser(id);
        return prepareResponse(OK, "User Deleted Successfully");
    }
    
    @PostMapping("/updateProfileImage")
    public ResponseEntity<User> updateProfileImage(@RequestParam("username") String username, @RequestParam(value = "profileImage") MultipartFile profileImage)
    		throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException {
        User user = userService.updateProfileImage(username, profileImage);
        return new ResponseEntity<>(user, OK);
    }

    @GetMapping(path = "/image/{username}/{fileName}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getProfileImage(@PathVariable("username") String username, @PathVariable("fileName") String fileName) throws IOException {
    	/**
    	 * "user.home" + "/supportportal/user/ramu/ramu.jpg" 
    	 */
    	Path profileImagePath = Paths.get(FileConstant.USER_FOLDER + username + FileConstant.FORWARD_SLASH + fileName);
    	return Files.readAllBytes(profileImagePath);
    }
 
    @GetMapping(path = "/image/profile/{username}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getTemporaryProfileImage(@PathVariable("username") String username) throws IOException {
    	/**
    	 * https://robohash.org/testuser
    	 */
    	URL url = new URL(FileConstant.TEMP_PROFILE_IMAGE_BASE_URL + username);
    	
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	try (InputStream inputStream = url.openStream()) {
    		int bytesRead; 
    		byte[] chunk = new byte[1024];
    		 while((bytesRead = inputStream.read(chunk)) > 0) {
    			 outputStream.write(chunk, 0, bytesRead);
             }
    	}
    	return outputStream.toByteArray();
    }
    

    
	private ResponseEntity<CustomHttpResponse> prepareResponse(HttpStatus httpStatus, String message) {
		CustomHttpResponse customHttpResponse = new CustomHttpResponse(new Date(), httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message);
		return new ResponseEntity<CustomHttpResponse>(customHttpResponse, httpStatus);
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

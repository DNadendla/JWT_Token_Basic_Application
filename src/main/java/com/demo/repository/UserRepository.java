package com.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

	public User findUserByUserName(String userName);
	
	public User findUserByEmail(String email);
}

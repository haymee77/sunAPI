package kr.co.sunpay.api.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import kr.co.sunpay.api.domain.User;
import kr.co.sunpay.api.repository.UserRepository;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	UserRepository userRepo;
	
	@Autowired
	PasswordEncoder pwEncoder;
	
	@PostMapping("")
	public ResponseEntity<Object> createUser(@RequestBody User user) {

		user.setPassword(pwEncoder.encode(user.getPassword()));
		
		User newUser = userRepo.save(user);
		
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uid}")
						.buildAndExpand(newUser.getUid()).toUri();
		
		return ResponseEntity.created(location).build();
	}
}

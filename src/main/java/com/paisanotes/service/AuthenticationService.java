package com.paisanotes.service;

import com.paisanotes.dto.AuthResponse;
import com.paisanotes.dto.LoginRequest;
import com.paisanotes.dto.RegisterRequest;
import com.paisanotes.entity.User;
import com.paisanotes.repository.UserRepository;
import com.paisanotes.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	public AuthResponse register(RegisterRequest request){
		if (userRepository.findByEmail(request.email()).isPresent()){
			throw new IllegalArgumentException("Email is already in use");
		}

		User user = new User();
		user.setName(request.name());
		user.setEmail(request.email());
		user.setPasswordHash(passwordEncoder.encode(request.password()));

		User savedUser = userRepository.save(user);

		org.springframework.security.core.userdetails.User springUser = new org.springframework.security.core.userdetails.User(
				savedUser.getEmail(), savedUser.getPasswordHash(), Collections.emptyList()
		);

		String jwtToken = jwtService.generateToken(springUser);

		return new AuthResponse(jwtToken, savedUser.getId(), savedUser.getName(), savedUser.getEmail());
	}

	public AuthResponse login(LoginRequest request){
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.email(), request.password())
		);

		User user = userRepository.findByEmail(request.email()).orElseThrow(
				() -> new IllegalArgumentException("User not found")
		);

		org.springframework.security.core.userdetails.User springUser = new org.springframework.security.core.userdetails.User(
				user.getEmail(), user.getPasswordHash(), Collections.emptyList()
		);

		String jwtToken = jwtService.generateToken(springUser);

		return new AuthResponse(jwtToken, user.getId(), user.getName(), user.getEmail());
	}
}

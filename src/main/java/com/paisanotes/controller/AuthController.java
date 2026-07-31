package com.paisanotes.controller;

import com.paisanotes.dto.*;
import com.paisanotes.entity.User;
import com.paisanotes.repository.UserRepository;
import com.paisanotes.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthenticationService authenticationService;
	private final UserRepository userRepository;

	@PostMapping("/google")
	public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request){
		try{
			return ResponseEntity.ok(authenticationService.googleLogin(request));
		}catch (Exception e){
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register (@Valid @RequestBody RegisterRequest request){
		try{
			AuthResponse response = authenticationService.register(request);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		}catch (IllegalArgumentException e){
			return ResponseEntity.badRequest().build();
		}
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		try {
			return ResponseEntity.ok(authenticationService.login(request));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		try {
			authenticationService.forgotPassword(request);
			return ResponseEntity.ok().build();
		} catch (IllegalArgumentException e) {
			// Security best practice: Even if email is not found, return 200 OK
			// so hackers can't use this endpoint to guess registered emails!
			return ResponseEntity.ok().build();
		}
	}

	@PostMapping("/reset-password")
	public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		try {
			authenticationService.resetPassword(request);
			return ResponseEntity.ok().build();
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@DeleteMapping("/account")
	public ResponseEntity<Void> deleteAccount(Authentication authentication){
		try {
			String email = authentication.getName();
			User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found."));

			userRepository.delete(user);

			return ResponseEntity.ok().build();
		}catch (Exception e){
			return ResponseEntity.badRequest().build();
		}
	}
}

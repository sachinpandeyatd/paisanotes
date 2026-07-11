package com.paisanotes.controller;

import com.paisanotes.dto.AuthResponse;
import com.paisanotes.dto.LoginRequest;
import com.paisanotes.dto.RegisterRequest;
import com.paisanotes.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthenticationService authenticationService;

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
}

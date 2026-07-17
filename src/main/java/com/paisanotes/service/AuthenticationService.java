package com.paisanotes.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.paisanotes.dto.AuthResponse;
import com.paisanotes.dto.GoogleLoginRequest;
import com.paisanotes.dto.LoginRequest;
import com.paisanotes.dto.RegisterRequest;
import com.paisanotes.entity.User;
import com.paisanotes.repository.UserRepository;
import com.paisanotes.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	@Value("${google.client-id}")
	private String googleClientId;

	public AuthResponse googleLogin(GoogleLoginRequest request){
		try {
			GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
					.setAudience(Collections.singletonList(googleClientId)).build();

			GoogleIdToken idToken = verifier.verify(request.idToken());
			if (idToken == null) {
				throw new IllegalArgumentException("Invalid Google ID token");
			}

			GoogleIdToken.Payload payload = idToken.getPayload();
			String email = payload.getEmail();
			String name = (String) payload.get("name");

			Optional<User> userOptional = userRepository.findByEmail(email);

			User user;

			if(userOptional.isPresent()){
				user = userOptional.get();
			}else {
				user = new User();
				user.setEmail(email);
				user.setName(name != null ? name : "Google User");

				String randomPassword = UUID.randomUUID().toString();
				user.setPasswordHash(passwordEncoder.encode(randomPassword));

				user = userRepository.save(user);
			}

			org.springframework.security.core.userdetails.User springUser =
					new org.springframework.security.core.userdetails.User(
						user.getEmail(), user.getPasswordHash(), Collections.emptyList()
					);

			String jwtToken = jwtService.generateToken(springUser);

			return new AuthResponse(jwtToken, user.getId(), user.getName(), user.getEmail());
		} catch (GeneralSecurityException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Google Authentication failed", e);
		}
	}

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

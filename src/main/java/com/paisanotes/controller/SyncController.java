package com.paisanotes.controller;

import com.paisanotes.dto.SyncPullResponse;
import com.paisanotes.dto.SyncPushRequest;
import com.paisanotes.dto.SyncPushResponse;
import com.paisanotes.entity.User;
import com.paisanotes.repository.UserRepository;
import com.paisanotes.service.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
public class SyncController {
	private final SyncService syncService;
	private final UserRepository userRepository;

	@GetMapping("/pull")
	public ResponseEntity<SyncPullResponse> pullData(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)ZonedDateTime lastSync,
			Authentication authentication
	){
		if (lastSync == null){
			lastSync = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC"));
		}

		String email = authentication.getName();

		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found."));

		SyncPullResponse response = syncService.pull(user.getId(), lastSync);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/push")
	public ResponseEntity<SyncPushResponse> pushData(@RequestBody SyncPushRequest request, Authentication authentication){
		String email = authentication.getName();
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		SyncPushResponse response = syncService.push(user.getId(), request);

		return ResponseEntity.ok(response);
	}
}

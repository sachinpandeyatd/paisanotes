package com.paisanotes.dto;

import java.util.UUID;

public record AuthResponse(
		String token,
		UUID id,
		String userId,
		String email
) {}

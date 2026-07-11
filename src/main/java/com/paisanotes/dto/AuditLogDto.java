package com.paisanotes.dto;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

public record AuditLogDto(
		UUID id,
		String entityType,
		UUID entityId,
		String actionType,
		Map<String, Object> metadata,
		ZonedDateTime createdAt
){}

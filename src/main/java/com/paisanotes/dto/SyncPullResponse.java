package com.paisanotes.dto;

import java.time.ZonedDateTime;
import java.util.List;

public record SyncPullResponse(
		ZonedDateTime serverTimestamp,
		List<TransactionDto> transactions,
		List<AuditLogDto> auditLogs
){}

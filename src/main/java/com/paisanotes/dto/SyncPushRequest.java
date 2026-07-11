package com.paisanotes.dto;

import java.util.List;

public record SyncPushRequest(
		List<TransactionDto> transactions,
		List<AuditLogDto> auditLogs
){}

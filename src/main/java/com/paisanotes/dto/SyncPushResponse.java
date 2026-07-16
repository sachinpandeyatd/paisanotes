package com.paisanotes.dto;

import java.util.List;
import java.util.UUID;

public record SyncPushResponse(
		List<UUID> processedTransactionIds,
		List<UUID> processedAuditLogIds,
		List<UUID> processedPersonIds,
		List<UUID> processedLoanIds,
		List<UUID> processedEmiIds
){}

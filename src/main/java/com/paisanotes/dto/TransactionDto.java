package com.paisanotes.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public record TransactionDto(
		UUID id,
		BigDecimal amount,
		String transactionType,
		String merchant,
		String category,
		ZonedDateTime transactionDate,
		String paymentMethod,
		String source,
		String notes,
		ZonedDateTime createdAt,
		ZonedDateTime updatedAt,
		boolean isDeleted
) {}

package com.paisanotes.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

public record LoanDto(
		UUID id, UUID personId, String type, BigDecimal amountLent, BigDecimal amountRepaid,
		LocalDate dateGiven, LocalDate expectedReturnDate, String status, String notes,
		ZonedDateTime createdAt, ZonedDateTime updatedAt, boolean isDeleted
) {}
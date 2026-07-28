package com.paisanotes.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

public record EmiDto(
		UUID id, UUID personId, String refNumber, String itemName, String ownerType,
		BigDecimal principalAmount, BigDecimal monthlyEmiAmount, Integer totalMonths, Integer completedMonths,
		BigDecimal totalAmountWithInterest, BigDecimal interestRate, BigDecimal amountPaid,
		LocalDate startDate, String status, ZonedDateTime createdAt, ZonedDateTime updatedAt, boolean isDeleted
) {}
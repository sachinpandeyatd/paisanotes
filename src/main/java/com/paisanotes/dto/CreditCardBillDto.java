package com.paisanotes.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

public record CreditCardBillDto(
    UUID id, UUID accountId, String billingMonth, BigDecimal totalBilledAmount, 
    BigDecimal minimumDue, BigDecimal amountPaid, LocalDate dueDate, String status, 
    ZonedDateTime createdAt, ZonedDateTime updatedAt, boolean isDeleted
) {}
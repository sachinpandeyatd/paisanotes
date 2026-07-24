package com.paisanotes.dto;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public record BudgetDto(
    UUID id, UUID categoryId, BigDecimal monthlyLimit, 
    ZonedDateTime createdAt, ZonedDateTime updatedAt, boolean isDeleted
) {}
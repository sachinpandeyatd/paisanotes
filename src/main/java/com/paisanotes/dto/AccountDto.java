package com.paisanotes.dto;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public record AccountDto(
    UUID id, String name, String type, BigDecimal initialBalance, 
    ZonedDateTime createdAt, ZonedDateTime updatedAt, boolean isDeleted
) {}
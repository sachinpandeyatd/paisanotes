package com.paisanotes.dto;
import java.time.ZonedDateTime;
import java.util.UUID;

public record PersonDto(
    UUID id, String name, String phoneNumber, ZonedDateTime createdAt, ZonedDateTime updatedAt, boolean isDeleted
) {}
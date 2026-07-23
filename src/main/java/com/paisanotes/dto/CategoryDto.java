package com.paisanotes.dto;
import java.time.ZonedDateTime;
import java.util.UUID;

public record CategoryDto(
    UUID id, String name, String icon, String color, boolean isDefault, 
    ZonedDateTime createdAt, ZonedDateTime updatedAt, boolean isDeleted
) {}
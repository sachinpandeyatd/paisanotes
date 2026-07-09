package com.paisanotes.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "audit_logs")
public class AuditLog {
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "entity_type", nullable = false)
	private String entityId;

	@Column(name = "action_type", nullable = false)
	private String actionType;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private Map<String, Object> metadata;

	@Column(name = "created_at", updatable = false)
	private ZonedDateTime createdAt;

	@PrePersist
	protected void onCreate(){
		if (createdAt == null) {
			createdAt = ZonedDateTime.now();
		}
	}
}

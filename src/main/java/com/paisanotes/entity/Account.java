package com.paisanotes.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "accounts")
@SQLRestriction("is_deleted = false")
public class Account extends BaseEntity{
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String type; // CASH, SAVINGS, CREDIT_CARD, WALLET

	@Column(name = "initial_balance", nullable = false, precision = 12, scale = 2)
	@Builder.Default
	private BigDecimal initialBalance = BigDecimal.ZERO;

	@Column(name = "statement_day")
	private Integer statementDay;

	@Column(name = "due_day")
	private Integer dueDay;
}

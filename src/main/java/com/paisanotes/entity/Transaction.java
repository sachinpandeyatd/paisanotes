package com.paisanotes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ManyToAny;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "transactions")
@SQLRestriction("is_deleted = false")
public class Transaction extends BaseEntity{

	@Id
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@Column(name = "transaction_type", nullable = false)
	private String transactionType;

	private String merchant;

	@Column(nullable = false)
	private String category;

	@Column(name = "category_id")
	private UUID categoryId;

	@Column(name = "transaction_date", nullable = false)
	private ZonedDateTime transactionDate;

	@Column(name = "payment_method", nullable = false)
	private String paymentMethod;

	@Column(nullable = false)
	private String source;

	@Column(columnDefinition = "TEXT")
	private String notes;
}

package com.paisanotes.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "installments")
public class Installment {
	@Id
	private UUID id;

	@Column(name = "reference_type", nullable = false)
	private String referenceType;

	@Column(name = "reference_id", nullable = false)
	private UUID referenceId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "transaction_id")
	private Transaction transaction;

	@Column(name = "amount_paid", nullable = false, precision = 12, scale = 2)
	private BigDecimal amountPaid;

	@Column(name = "payment_date", nullable = false)
	private ZonedDateTime paymentDate;
}

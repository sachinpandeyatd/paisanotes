package com.paisanotes.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "loans")
@SQLRestriction("is_deleted = false")
public class Loan extends BaseEntity{
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "person_id", nullable = false)
	private Person person;

	@Column(name = "amount_lent", nullable = false, precision = 12, scale = 2)
	private BigDecimal amountLent;

	@Column(name = "date_given", nullable = false)
	private LocalDate dateGiven;

	@Column(name = "expected_return_date")
	private LocalDate expectedReturnDate;

	@Column(nullable = false)
	@Builder.Default
	private String status = "ACTIVE";

	@Column(columnDefinition = "TEXT")
	private String notes;
}

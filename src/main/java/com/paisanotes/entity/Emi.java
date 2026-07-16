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
@Table(name = "emis")
@SQLRestriction("is_deleted = false")
public class Emi extends BaseEntity{

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "person_id")
	private Person person;

	@Column(name = "ref_number")
	private String refNumber;

	@Column(name = "item_name", nullable = false)
	private String itemName;

	@Column(name = "owner_type", nullable = false)
	private String ownerType;

	@Column(name = "principal_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal principalAmount;

	@Column(name = "monthly_emi_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal monthlyEmiAmount;

	@Column(name = "total_months", nullable = false)
	private Integer totalMonths;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	@Builder.Default
	private String status = "ACTIVE";

	@Column(name = "completed_months")
	@Builder.Default
	private Integer completedMonths = 0;
}

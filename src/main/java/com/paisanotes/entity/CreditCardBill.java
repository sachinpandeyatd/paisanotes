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
@Table(name = "credit_card_bills")
@SQLRestriction("is_deleted = false")
public class CreditCardBill extends BaseEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "billing_month", nullable = false)
    private String billingMonth;

    @Column(name = "total_billed_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalBilledAmount = BigDecimal.ZERO;

    @Column(name = "minimum_due", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal minimumDue = BigDecimal.ZERO;

    @Column(name = "amount_paid", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(nullable = false)
    @Builder.Default
    private String status = "UNPAID";
}
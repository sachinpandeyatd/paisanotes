package com.paisanotes.entity;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "parsing_rules")
public class ParsingRule {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "package_name", nullable = false)
	private String packageName;

	@Column(name = "regex_pattern", nullable = false, length = 1000)
	private String regexPattern;

	@Column(name = "transaction_type", nullable = false)
	private String transactionType;

	@Column(nullable = false)
	@Builder.Default
	private Integer version = 1;
}

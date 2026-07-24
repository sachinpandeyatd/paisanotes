package com.paisanotes.dto;

import java.util.List;

public record SyncPushRequest(
		List<TransactionDto> transactions,
		List<AuditLogDto> auditLogs,
		List<PersonDto> people,
		List<LoanDto> loans,
		List<EmiDto> emis,
		List<CategoryDto> categories,
		List<BudgetDto> budgets
){}

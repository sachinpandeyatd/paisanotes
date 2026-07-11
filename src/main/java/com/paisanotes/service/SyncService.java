package com.paisanotes.service;

import com.paisanotes.dto.SyncPullResponse;
import com.paisanotes.dto.TransactionDto;
import com.paisanotes.entity.Transaction;
import com.paisanotes.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SyncService {
	private final TransactionRepository transactionRepository;

	public SyncPullResponse pull(UUID id, ZonedDateTime lastSyncTime){
		List<Transaction> changedTransactions = transactionRepository.findModifiedAfter(id, lastSyncTime);
		List<TransactionDto> transactionDtos = changedTransactions.stream()
				.map(this::mapToDto).collect(Collectors.toList());

		return new SyncPullResponse(ZonedDateTime.now(ZoneId.of("UTC")), transactionDtos);
	}

	private TransactionDto mapToDto(Transaction entity) {
		return new TransactionDto(
				entity.getId(),
				entity.getAmount(),
				entity.getTransactionType(),
				entity.getMerchant(),
				entity.getCategory(),
				entity.getTransactionDate(),
				entity.getPaymentMethod(),
				entity.getSource(),
				entity.getNotes(),
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.isDeleted()
		);
	}
}

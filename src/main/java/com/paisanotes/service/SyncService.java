package com.paisanotes.service;

import com.paisanotes.dto.SyncPullResponse;
import com.paisanotes.dto.SyncPushRequest;
import com.paisanotes.dto.SyncPushResponse;
import com.paisanotes.dto.TransactionDto;
import com.paisanotes.entity.Transaction;
import com.paisanotes.entity.User;
import com.paisanotes.repository.TransactionRepository;
import com.paisanotes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SyncService {
	private final TransactionRepository transactionRepository;
	private final UserRepository userRepository;

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


	public SyncPushResponse push (UUID userId, SyncPushRequest request){
		List<TransactionDto> incomingTransactions = request.transactions();

		if (incomingTransactions == null || incomingTransactions.isEmpty()){
			return new SyncPushResponse(List.of());
		}

		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

		List<UUID> incomingIds = incomingTransactions.stream().map(TransactionDto::id).collect(Collectors.toList());

		Map<UUID, Transaction> existingTransactionMap = transactionRepository.findAllById(incomingIds)
				.stream().collect(Collectors.toMap(Transaction::getId, Function.identity()));

		List<Transaction> transactionsToSave = new ArrayList<>();
		List<UUID> successfullyProcessedIds = new ArrayList<>();

		for (TransactionDto incomingDto : incomingTransactions){
			Transaction existingTransaction = existingTransactionMap.get(incomingDto.id());

			if (existingTransaction != null){
				if (incomingDto.updatedAt().isAfter(existingTransaction.getUpdatedAt())) {
					updateEntityFromDto(existingTransaction, incomingDto);
					transactionsToSave.add(existingTransaction);
					successfullyProcessedIds.add(incomingDto.id());
				} else {
					successfullyProcessedIds.add(incomingDto.id());
				}
			}else {
				Transaction newTransaction = createEntityFromDto(incomingDto, user);
				transactionsToSave.add(newTransaction);
				successfullyProcessedIds.add(incomingDto.id());
			}
		}
		transactionRepository.saveAll(transactionsToSave);
		return new SyncPushResponse(successfullyProcessedIds);
	}

	private Transaction createEntityFromDto(TransactionDto dto, User user) {
		Transaction entity = new Transaction();
		entity.setId(dto.id());
		entity.setUser(user);
		updateEntityFromDto(entity, dto);

		entity.setCreatedAt(dto.createdAt());
		return entity;
	}

	private void updateEntityFromDto(Transaction entity, TransactionDto dto) {
		entity.setAmount(dto.amount());
		entity.setTransactionType(dto.transactionType());
		entity.setMerchant(dto.merchant());
		entity.setCategory(dto.category());
		entity.setTransactionDate(dto.transactionDate());
		entity.setPaymentMethod(dto.paymentMethod());
		entity.setSource(dto.source());
		entity.setNotes(dto.notes());
		entity.setDeleted(dto.isDeleted());
		entity.setUpdatedAt(dto.updatedAt());
	}
}

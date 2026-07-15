package com.paisanotes.service;

import com.paisanotes.dto.*;
import com.paisanotes.entity.AuditLog;
import com.paisanotes.entity.Transaction;
import com.paisanotes.entity.User;
import com.paisanotes.repository.AuditLogRepository;
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
	private final AuditLogRepository auditLogRepository;

	public SyncPullResponse pull(UUID id, ZonedDateTime lastSyncTime){
		List<Transaction> changedTransactions = transactionRepository.findModifiedAfter(id, lastSyncTime);
		List<TransactionDto> transactionDtos = changedTransactions.stream()
				.map(this::mapToDto).collect(Collectors.toList());

		List<AuditLog> newLogs = auditLogRepository.findByUserIdAndCreatedAtGreaterThan(id, lastSyncTime);
		List<AuditLogDto> logDtos = newLogs.stream().map(this::mapLogToDto).collect(Collectors.toList());

		return new SyncPullResponse(ZonedDateTime.now(ZoneId.of("UTC")), transactionDtos, logDtos);
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


	public SyncPushResponse push(UUID userId, SyncPushRequest request){

		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

		List<UUID> processedTxnIds = processTransactions(request.transactions(), user);

		List<UUID> processedLogIds = processAuditLogs(request.auditLogs(),  user);

		return new SyncPushResponse(processedTxnIds, processedLogIds);
	}

	private List<UUID> processAuditLogs(List<AuditLogDto> incomingLogs, User user) {
		if (incomingLogs == null || incomingLogs.isEmpty()) return List.of();

		List<AuditLog> logsToSave = new ArrayList<>();
		List<UUID> processedIds = new ArrayList<>();

		List<UUID> incomingIds = incomingLogs.stream().map(AuditLogDto::id).collect(Collectors.toList());

		Map<UUID, AuditLog> existingLogs = auditLogRepository.findAllById(incomingIds).stream()
				.collect(Collectors.toMap(AuditLog::getId, Function.identity()));

		for (AuditLogDto dto : incomingLogs){
			if (!existingLogs.containsKey(dto.id())){
				AuditLog log = new AuditLog();
				log.setId(dto.id());
				log.setUser(user);
				log.setEntityType(dto.entityType());
				log.setEntityId(dto.entityId());
				log.setActionType(dto.actionType());
				log.setMetadata(dto.metadata()); // Native JSONB mapping!
				log.setCreatedAt(dto.createdAt());

				logsToSave.add(log);
			}

			processedIds.add(dto.id());
		}

		auditLogRepository.saveAll(logsToSave);
		return processedIds;
	}

	private List<UUID> processTransactions(List<TransactionDto> dtos, User user) {
		if (dtos == null || dtos.isEmpty()) {
			return List.of();
		}

		List<UUID> incomingIds = dtos.stream()
				.map(TransactionDto::id)
				.collect(Collectors.toList());

		Map<UUID, Transaction> existingTransactionsMap = transactionRepository.findAllById(incomingIds)
				.stream()
				.collect(Collectors.toMap(Transaction::getId, Function.identity()));

		List<Transaction> transactionsToSave = new ArrayList<>();
		List<UUID> successfullyProcessedIds = new ArrayList<>();

		for (TransactionDto incomingDto : dtos) {

			Transaction existingTransaction = existingTransactionsMap.get(incomingDto.id());

			if (existingTransaction != null) {
				if (incomingDto.updatedAt().isAfter(existingTransaction.getUpdatedAt())) {
					updateEntityFromDto(existingTransaction, incomingDto);
					transactionsToSave.add(existingTransaction);
					successfullyProcessedIds.add(incomingDto.id());
				} else {
					successfullyProcessedIds.add(incomingDto.id());
				}
			} else {
				Transaction newTransaction = createEntityFromDto(incomingDto, user);
				transactionsToSave.add(newTransaction);
				successfullyProcessedIds.add(incomingDto.id());
			}
		}

		transactionRepository.saveAll(transactionsToSave);

		return successfullyProcessedIds;
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

	private AuditLogDto mapLogToDto(AuditLog entity){
		return new AuditLogDto(
				entity.getId(),
				entity.getEntityType(),
				entity.getEntityId(),
				entity.getActionType(),
				entity.getMetadata(),
				entity.getCreatedAt()
		);
	}
}

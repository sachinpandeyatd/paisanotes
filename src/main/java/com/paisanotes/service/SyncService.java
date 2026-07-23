package com.paisanotes.service;

import com.paisanotes.dto.*;
import com.paisanotes.entity.*;
import com.paisanotes.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
	private final PersonRepository personRepository;
	private final LoanRepository loanRepository;
	private final EmiRepository emiRepository;
	private final AuditLogRepository auditLogRepository;
	private final UserRepository userRepository;
	private final CategoryRepository categoryRepository;


	public SyncPullResponse pull(UUID userId, ZonedDateTime lastSyncTime) {

		List<TransactionDto> txns = transactionRepository.findModifiedAfter(userId, lastSyncTime).stream()
				.map(this::mapTransactionToDto).toList();

		List<PersonDto> people = personRepository.findModifiedAfter(userId, lastSyncTime).stream()
				.map(this::mapPersonToDto).toList();

		List<LoanDto> loans = loanRepository.findModifiedAfter(userId, lastSyncTime).stream()
				.map(this::mapLoanToDto).toList();

		List<EmiDto> emis = emiRepository.findModifiedAfter(userId, lastSyncTime).stream()
				.map(this::mapEmiToDto).toList();

		List<AuditLogDto> logs = auditLogRepository.findByUserIdAndCreatedAtGreaterThan(userId, lastSyncTime).stream()
				.map(this::mapLogToDto).toList();

		List<CategoryDto> categories = categoryRepository.findModifiedAfter(userId, lastSyncTime).stream()
				.map(e -> new CategoryDto(e.getId(), e.getName(), e.getIcon(), e.getColor(), e.isDefault(), e.getCreatedAt(), e.getUpdatedAt(), e.isDeleted()))
				.toList();

		return new SyncPullResponse(ZonedDateTime.now(ZoneId.of("UTC")), txns, logs, people, loans, emis, categories);
	}

	@Transactional
	public SyncPushResponse push(UUID userId, SyncPushRequest request) {
		User user = userRepository.findById(userId).orElseThrow();

		List<UUID> processedTxns = processTransactions(request.transactions(), user);
		List<UUID> processedLogs = processAuditLogs(request.auditLogs(), user);
		List<UUID> processedPeople = processPeople(request.people(), user);
		List<UUID> processedLoans = processLoans(request.loans(), user);
		List<UUID> processedEmis = processEmis(request.emis(), user);
		List<UUID> processedCategories = processCategories(request.categories(), user);

		return new SyncPushResponse(processedTxns, processedLogs, processedPeople, processedLoans, processedEmis);
	}

	private List<UUID> processCategories(List<CategoryDto> dtos, User user) {
		if (dtos == null || dtos.isEmpty()) return List.of();
		Map<UUID, Category> existingMap = categoryRepository.findAllById(dtos.stream().map(CategoryDto::id).toList()).stream()
				.collect(Collectors.toMap(Category::getId, Function.identity()));
		List<Category> toSave = new ArrayList<>();
		List<UUID> processedIds = new ArrayList<>();

		for (CategoryDto dto : dtos) {
			Category existing = existingMap.get(dto.id());
			if (existing != null) {
				if (dto.updatedAt().isAfter(existing.getUpdatedAt())) {
					existing.setName(dto.name());
					existing.setIcon(dto.icon());
					existing.setColor(dto.color());
					existing.setDefault(dto.isDefault());
					existing.setDeleted(dto.isDeleted());
					existing.setUpdatedAt(dto.updatedAt());
					toSave.add(existing);
				}
			} else {
				Category newCat = new Category();
				newCat.setId(dto.id());
				newCat.setUser(user);
				newCat.setName(dto.name());
				newCat.setIcon(dto.icon());
				newCat.setColor(dto.color());
				newCat.setDefault(dto.isDefault());
				newCat.setCreatedAt(dto.createdAt());
				newCat.setUpdatedAt(dto.updatedAt());
				newCat.setDeleted(dto.isDeleted());
				toSave.add(newCat);
			}
			processedIds.add(dto.id());
		}
		categoryRepository.saveAll(toSave);
		return processedIds;
	}

	private List<UUID> processTransactions(List<TransactionDto> dtos, User user) {
		if (dtos == null || dtos.isEmpty()) return List.of();
		Map<UUID, Transaction> existingMap = transactionRepository.findAllById(dtos.stream().map(TransactionDto::id).toList()).stream()
				.collect(Collectors.toMap(Transaction::getId, Function.identity()));
		List<Transaction> toSave = new ArrayList<>();
		List<UUID> processedIds = new ArrayList<>();

		for (TransactionDto dto : dtos) {
			Transaction existing = existingMap.get(dto.id());
			if (existing != null) {
				if (dto.updatedAt().isAfter(existing.getUpdatedAt())) {
					existing.setAmount(dto.amount());
					existing.setTransactionType(dto.transactionType());
					existing.setMerchant(dto.merchant());
					existing.setCategory(dto.category());
					existing.setTransactionDate(dto.transactionDate());
					existing.setPaymentMethod(dto.paymentMethod());
					existing.setSource(dto.source());
					existing.setNotes(dto.notes());
					existing.setDeleted(dto.isDeleted());
					existing.setUpdatedAt(dto.updatedAt());
					toSave.add(existing);
				}
			} else {
				Transaction newTxn = new Transaction();
				newTxn.setId(dto.id());
				newTxn.setUser(user);
				newTxn.setAmount(dto.amount());
				newTxn.setTransactionType(dto.transactionType());
				newTxn.setMerchant(dto.merchant());
				newTxn.setCategory(dto.category());
				newTxn.setTransactionDate(dto.transactionDate());
				newTxn.setPaymentMethod(dto.paymentMethod());
				newTxn.setSource(dto.source());
				newTxn.setNotes(dto.notes());
				newTxn.setCreatedAt(dto.createdAt());
				newTxn.setUpdatedAt(dto.updatedAt());
				newTxn.setDeleted(dto.isDeleted());
				toSave.add(newTxn);
			}
			processedIds.add(dto.id());
		}
		transactionRepository.saveAll(toSave);
		return processedIds;
	}

	private List<UUID> processAuditLogs(List<AuditLogDto> dtos, User user) {
		if (dtos == null || dtos.isEmpty()) return List.of();
		List<UUID> incomingIds = dtos.stream().map(AuditLogDto::id).toList();
		Map<UUID, AuditLog> existingLogs = auditLogRepository.findAllById(incomingIds).stream()
				.collect(Collectors.toMap(AuditLog::getId, Function.identity()));
		List<AuditLog> logsToSave = new ArrayList<>();
		List<UUID> processedIds = new ArrayList<>();

		for (AuditLogDto dto : dtos) {
			if (!existingLogs.containsKey(dto.id())) {
				AuditLog log = new AuditLog();
				log.setId(dto.id());
				log.setUser(user);
				log.setEntityType(dto.entityType());
				log.setEntityId(dto.entityId());
				log.setActionType(dto.actionType());
				log.setMetadata(dto.metadata());
				log.setCreatedAt(dto.createdAt());
				logsToSave.add(log);
			}
			processedIds.add(dto.id());
		}
		auditLogRepository.saveAll(logsToSave);
		return processedIds;
	}

	private List<UUID> processPeople(List<PersonDto> dtos, User user) {
		if (dtos == null || dtos.isEmpty()) return List.of();
		Map<UUID, Person> existingMap = personRepository.findAllById(dtos.stream().map(PersonDto::id).toList()).stream()
				.collect(Collectors.toMap(Person::getId, Function.identity()));
		List<Person> toSave = new ArrayList<>();
		List<UUID> processedIds = new ArrayList<>();

		for (PersonDto dto : dtos) {
			Person existing = existingMap.get(dto.id());
			if (existing != null) {
				if (dto.updatedAt().isAfter(existing.getUpdatedAt())) {
					existing.setName(dto.name());
					existing.setPhoneNumber(dto.phoneNumber());
					existing.setDeleted(dto.isDeleted());
					existing.setUpdatedAt(dto.updatedAt());
					toSave.add(existing);
				}
			} else {
				Person newPerson = new Person();
				newPerson.setId(dto.id());
				newPerson.setUser(user);
				newPerson.setName(dto.name());
				newPerson.setPhoneNumber(dto.phoneNumber());
				newPerson.setCreatedAt(dto.createdAt());
				newPerson.setUpdatedAt(dto.updatedAt());
				newPerson.setDeleted(dto.isDeleted());
				toSave.add(newPerson);
			}
			processedIds.add(dto.id());
		}
		personRepository.saveAll(toSave);
		return processedIds;
	}

	private List<UUID> processLoans(List<LoanDto> dtos, User user) {
		if (dtos == null || dtos.isEmpty()) return List.of();
		Map<UUID, Loan> existingMap = loanRepository.findAllById(dtos.stream().map(LoanDto::id).toList()).stream()
				.collect(Collectors.toMap(Loan::getId, Function.identity()));
		List<Loan> toSave = new ArrayList<>();
		List<UUID> processedIds = new ArrayList<>();

		for (LoanDto dto : dtos) {
			Loan existing = existingMap.get(dto.id());
			if (existing != null) {
				if (dto.updatedAt().isAfter(existing.getUpdatedAt())) {
					existing.setAmountLent(dto.amountLent());
					existing.setDateGiven(dto.dateGiven());
					existing.setExpectedReturnDate(dto.expectedReturnDate());
					existing.setStatus(dto.status());
					existing.setNotes(dto.notes());
					existing.setDeleted(dto.isDeleted());
					existing.setUpdatedAt(dto.updatedAt());
					existing.setType(dto.type());
					existing.setAmountRepaid(dto.amountRepaid());
					toSave.add(existing);
				}
			} else {
				Loan newLoan = new Loan();
				newLoan.setId(dto.id());
				newLoan.setUser(user);
				newLoan.setPerson(personRepository.getReferenceById(dto.personId()));
				newLoan.setAmountLent(dto.amountLent());
				newLoan.setDateGiven(dto.dateGiven());
				newLoan.setExpectedReturnDate(dto.expectedReturnDate());
				newLoan.setStatus(dto.status());
				newLoan.setNotes(dto.notes());
				newLoan.setCreatedAt(dto.createdAt());
				newLoan.setUpdatedAt(dto.updatedAt());
				newLoan.setDeleted(dto.isDeleted());
				newLoan.setType(dto.type() != null ? dto.type() : "LENT");
				newLoan.setAmountRepaid(dto.amountRepaid() != null ? dto.amountRepaid() : BigDecimal.ZERO);
				toSave.add(newLoan);
			}
			processedIds.add(dto.id());
		}
		loanRepository.saveAll(toSave);
		return processedIds;
	}

	private List<UUID> processEmis(List<EmiDto> dtos, User user) {
		if (dtos == null || dtos.isEmpty()) return List.of();
		Map<UUID, Emi> existingMap = emiRepository.findAllById(dtos.stream().map(EmiDto::id).toList()).stream()
				.collect(Collectors.toMap(Emi::getId, Function.identity()));
		List<Emi> toSave = new ArrayList<>();
		List<UUID> processedIds = new ArrayList<>();

		for (EmiDto dto : dtos) {
			Emi existing = existingMap.get(dto.id());
			if (existing != null) {
				if (dto.updatedAt().isAfter(existing.getUpdatedAt())) {
					existing.setRefNumber(dto.refNumber());
					existing.setItemName(dto.itemName());
					existing.setOwnerType(dto.ownerType());
					existing.setPrincipalAmount(dto.principalAmount());
					existing.setMonthlyEmiAmount(dto.monthlyEmiAmount());
					existing.setTotalMonths(dto.totalMonths());
					existing.setStartDate(dto.startDate());
					existing.setStatus(dto.status());
					existing.setDeleted(dto.isDeleted());
					existing.setUpdatedAt(dto.updatedAt());
					existing.setCompletedMonths(dto.completedMonths());
					toSave.add(existing);
				}
			} else {
				Emi newEmi = new Emi();
				newEmi.setId(dto.id());
				newEmi.setUser(user);
				if (dto.personId() != null) newEmi.setPerson(personRepository.getReferenceById(dto.personId()));
				newEmi.setRefNumber(dto.refNumber());
				newEmi.setItemName(dto.itemName());
				newEmi.setOwnerType(dto.ownerType());
				newEmi.setPrincipalAmount(dto.principalAmount());
				newEmi.setMonthlyEmiAmount(dto.monthlyEmiAmount());
				newEmi.setTotalMonths(dto.totalMonths());
				newEmi.setStartDate(dto.startDate());
				newEmi.setStatus(dto.status());
				newEmi.setCreatedAt(dto.createdAt());
				newEmi.setUpdatedAt(dto.updatedAt());
				newEmi.setDeleted(dto.isDeleted());
				newEmi.setCompletedMonths(dto.completedMonths() != null ? dto.completedMonths() : 0);
				toSave.add(newEmi);
			}
			processedIds.add(dto.id());
		}
		emiRepository.saveAll(toSave);
		return processedIds;
	}


	private TransactionDto mapTransactionToDto(Transaction e) {
		return new TransactionDto(e.getId(), e.getAmount(), e.getTransactionType(), e.getMerchant(), e.getCategory(), e.getCategoryId(), e.getTransactionDate(), e.getPaymentMethod(), e.getSource(), e.getNotes(), e.getCreatedAt(), e.getUpdatedAt(), e.isDeleted());
	}

	private AuditLogDto mapLogToDto(AuditLog e) {
		return new AuditLogDto(e.getId(), e.getEntityType(), e.getEntityId(), e.getActionType(), e.getMetadata(), e.getCreatedAt());
	}

	private PersonDto mapPersonToDto(Person e) {
		return new PersonDto(e.getId(), e.getName(), e.getPhoneNumber(), e.getCreatedAt(), e.getUpdatedAt(), e.isDeleted());
	}

	private LoanDto mapLoanToDto(Loan e) {
		return new LoanDto(e.getId(), e.getPerson().getId(), e.getType(), e.getAmountLent(), e.getAmountRepaid(), e.getDateGiven(), e.getExpectedReturnDate(), e.getStatus(), e.getNotes(), e.getCreatedAt(), e.getUpdatedAt(), e.isDeleted());
	}

	private EmiDto mapEmiToDto(Emi e) {
		return new EmiDto(e.getId(), e.getPerson() != null ? e.getPerson().getId() : null, e.getRefNumber(), e.getItemName(), e.getOwnerType(), e.getPrincipalAmount(), e.getMonthlyEmiAmount(), e.getTotalMonths(), e.getCompletedMonths(), e.getStartDate(), e.getStatus(), e.getCreatedAt(), e.getUpdatedAt(), e.isDeleted());
	}
}
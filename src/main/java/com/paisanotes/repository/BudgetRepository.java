package com.paisanotes.repository;

import com.paisanotes.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    @Query(value = "SELECT * FROM budgets WHERE user_id = :userId AND updated_at > :lastSync", nativeQuery = true)
    List<Budget> findModifiedAfter(@Param("userId") UUID userId, @Param("lastSync") ZonedDateTime lastSync);
}
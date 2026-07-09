package com.paisanotes.repository;

import com.paisanotes.entity.Emi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface EmiRepository extends JpaRepository<Emi, UUID> {
	@Query(value = "SELECT * FROM emis WHERE user_id = :userId AND updated_at > :lastSync", nativeQuery = true)
	List<Emi> findModifiedAfter(@Param("userId") UUID userId, @Param("lastSync")ZonedDateTime lastSync);
}

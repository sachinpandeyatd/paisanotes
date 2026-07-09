package com.paisanotes.repository;

import com.paisanotes.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface PersonRepository extends JpaRepository<Person, UUID> {
	@Query(value = "SELECT * FROM people WHERE user_id = :userId AND updated_at > :lastSync", nativeQuery = true)
	List<Person> findModifiedAfter(@Param("userId") UUID userId, @Param("lastSync")ZonedDateTime lastSync);
}

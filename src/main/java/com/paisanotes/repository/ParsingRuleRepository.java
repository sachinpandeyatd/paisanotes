package com.paisanotes.repository;

import com.paisanotes.entity.ParsingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParsingRuleRepository extends JpaRepository<ParsingRule, Integer> {
    // Mobile app fetches all rules on startup to cache locally
}
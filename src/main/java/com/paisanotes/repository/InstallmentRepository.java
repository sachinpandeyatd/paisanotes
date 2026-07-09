package com.paisanotes.repository;

import com.paisanotes.entity.Installment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InstallmentRepository extends JpaRepository<Installment, UUID> {
    // Installments will be fetched through relationships or specific reference IDs
}
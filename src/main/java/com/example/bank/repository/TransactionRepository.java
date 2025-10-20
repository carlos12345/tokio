package com.example.bank.repository;

import com.example.bank.model.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionRecord, Long> {
  List<TransactionRecord> findByStatus(String status);
}

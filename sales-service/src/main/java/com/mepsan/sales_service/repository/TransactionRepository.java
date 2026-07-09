package com.mepsan.sales_service.repository;


import com.mepsan.sales_service.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    boolean existsByTransactionUuid(String transactionUuid);
}

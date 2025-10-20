package com.example.bank.service;

import com.example.bank.dto.CreateAccountRequest;
import com.example.bank.dto.OperationRequest;
import com.example.bank.model.Account;
import com.example.bank.model.TransactionRecord;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.TransactionRepository;
import com.example.bank.sqs.SqsSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {

  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;
  private final SqsSender sqsSender;

  @Transactional
  public Account create(CreateAccountRequest req) {
    if (accountRepository.findByNumber(req.getNumber()).isPresent())
      throw new RuntimeException("Account exists");
    Account acc = new Account();
    acc.setNumber(req.getNumber());
    acc.setOwner(req.getOwner());
    acc.setBalance(BigDecimal.ZERO);
    return accountRepository.save(acc);
  }

  @Transactional
  public TransactionRecord deposit(OperationRequest op) {
    Account acc = accountRepository.findByNumber(op.getAccountNumber())
            .orElseThrow(() -> new RuntimeException("Account not found"));
    acc.setBalance(acc.getBalance().add(op.getAmount()));
    accountRepository.save(acc);

    TransactionRecord tx = new TransactionRecord();
    tx.setAccountId(acc.getId());
    tx.setAmount(op.getAmount());
    tx.setType("DEPOSIT");
    tx.setStatus("PROCESSED");
    tx = transactionRepository.save(tx);

    // send event
    sqsSender.sendMessage(tx);
    return tx;
  }

  @Transactional
  public TransactionRecord withdraw(OperationRequest op) {
    Account acc = accountRepository.findByNumber(op.getAccountNumber())
            .orElseThrow(() -> new RuntimeException("Account not found"));
    if (acc.getBalance().compareTo(op.getAmount()) < 0) throw new RuntimeException("Insufficient funds");
    acc.setBalance(acc.getBalance().subtract(op.getAmount()));
    accountRepository.save(acc);

    TransactionRecord tx = new TransactionRecord();
    tx.setAccountId(acc.getId());
    tx.setAmount(op.getAmount());
    tx.setType("WITHDRAWAL");
    tx.setStatus("PROCESSED");
    tx = transactionRepository.save(tx);

    sqsSender.sendMessage(tx);
    return tx;
  }

  public Account findByNumber(String number) {
    return accountRepository.findByNumber(number).orElseThrow(() -> new RuntimeException("Account not found"));
  }
}

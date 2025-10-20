package com.example.bank.controller;

import com.example.bank.dto.CreateAccountRequest;
import com.example.bank.dto.OperationRequest;
import com.example.bank.model.Account;
import com.example.bank.model.TransactionRecord;
import com.example.bank.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

  private final AccountService accountService;

  @PostMapping
  public ResponseEntity<Account> create(@Valid @RequestBody CreateAccountRequest req) {
    Account c = accountService.create(req);
    return ResponseEntity.status(HttpStatus.CREATED).body(c);
  }

  @PostMapping("/deposit")
  public ResponseEntity<TransactionRecord> deposit(@Valid @RequestBody OperationRequest op) {
    return ResponseEntity.ok(accountService.deposit(op));
  }

  @PostMapping("/withdraw")
  public ResponseEntity<TransactionRecord> withdraw(@Valid @RequestBody OperationRequest op) {
    return ResponseEntity.ok(accountService.withdraw(op));
  }

  @GetMapping("/{number}")
  public ResponseEntity<Account> getByNumber(@PathVariable String number) {
    return ResponseEntity.ok(accountService.findByNumber(number));
  }
}

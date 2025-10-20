package com.example.bank;

import com.example.bank.dto.AuthRequest;
import com.example.bank.dto.AuthResponse;
import com.example.bank.dto.CreateAccountRequest;
import com.example.bank.dto.OperationRequest;
import com.example.bank.model.Account;
import com.example.bank.model.TransactionRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountIntegrationTest extends IntegrationTestBase {

  @Autowired
  private TestRestTemplate restTemplate;

  private String token;

  @BeforeEach
  void setupAuth() {
    AuthRequest req = new AuthRequest();
    req.setUsername("userbank");
    req.setPassword("123456");
    restTemplate.postForEntity("/api/auth/register", req, AuthResponse.class);
    ResponseEntity<AuthResponse> login = restTemplate.postForEntity("/api/auth/login", req, AuthResponse.class);
    token = "Bearer " + login.getBody().getToken();
  }

  @Test
  void deveCriarDepositarESacarConta() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", token);
    headers.setContentType(MediaType.APPLICATION_JSON);

    CreateAccountRequest createReq = new CreateAccountRequest();
    createReq.setNumber("0001-0001");
    createReq.setOwner("Fulano");
    HttpEntity<CreateAccountRequest> entity = new HttpEntity<>(createReq, headers);

    ResponseEntity<Account> createResp = restTemplate.exchange("/api/accounts", HttpMethod.POST, entity, Account.class);
    assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(createResp.getBody().getBalance()).isEqualByComparingTo("0");

    OperationRequest depositReq = new OperationRequest();
    depositReq.setAccountNumber("0001-0001");
    depositReq.setAmount(BigDecimal.valueOf(200));
    HttpEntity<OperationRequest> depositEntity = new HttpEntity<>(depositReq, headers);
    ResponseEntity<TransactionRecord> depositResp = restTemplate.exchange("/api/accounts/deposit", HttpMethod.POST, depositEntity, TransactionRecord.class);
    assertThat(depositResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(depositResp.getBody().getType()).isEqualTo("DEPOSIT");

    OperationRequest withdrawReq = new OperationRequest();
    withdrawReq.setAccountNumber("0001-0001");
    withdrawReq.setAmount(BigDecimal.valueOf(50));
    HttpEntity<OperationRequest> withdrawEntity = new HttpEntity<>(withdrawReq, headers);
    ResponseEntity<TransactionRecord> withdrawResp = restTemplate.exchange("/api/accounts/withdraw", HttpMethod.POST, withdrawEntity, TransactionRecord.class);
    assertThat(withdrawResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(withdrawResp.getBody().getType()).isEqualTo("WITHDRAWAL");
  }
}

package com.example.bank;

import com.example.bank.dto.AuthRequest;
import com.example.bank.dto.AuthResponse;
import com.example.bank.dto.CreateAccountRequest;
import com.example.bank.dto.OperationRequest;
import com.example.bank.model.TransactionRecord;
import com.example.bank.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SqsIntegrationTest extends IntegrationTestBase {

  @Autowired
  private TestRestTemplate restTemplate;

  @BeforeEach
  void setupAuthAndQueue() {
    AuthRequest req = new AuthRequest();
    req.setUsername("sqsuser");
    req.setPassword("pass123");
    restTemplate.postForEntity("/api/auth/register", req, AuthResponse.class);
  }

  @Test
  void deposit_should_send_message_to_sqs() throws InterruptedException {
    // login
    AuthRequest loginReq = new AuthRequest();
    loginReq.setUsername("sqsuser");
    loginReq.setPassword("pass123");
    ResponseEntity<AuthResponse> login = restTemplate.postForEntity("/api/auth/login", loginReq, AuthResponse.class);
    String token = "Bearer " + login.getBody().getToken();

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", token);
    headers.setContentType(MediaType.APPLICATION_JSON);

    // create account
    CreateAccountRequest createReq = new CreateAccountRequest();
    createReq.setNumber("1000-2000");
    createReq.setOwner("Test SQS");
    HttpEntity<CreateAccountRequest> entity = new HttpEntity<>(createReq, headers);
    ResponseEntity<Account> createResp = restTemplate.exchange("/api/accounts", HttpMethod.POST, entity, Account.class);
    assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    // deposit (this triggers SqsSender)
    OperationRequest depositReq = new OperationRequest();
    depositReq.setAccountNumber("1000-2000");
    depositReq.setAmount(BigDecimal.valueOf(123));
    HttpEntity<OperationRequest> depositEntity = new HttpEntity<>(depositReq, headers);
    ResponseEntity<TransactionRecord> depositResp = restTemplate.exchange("/api/accounts/deposit", HttpMethod.POST, depositEntity, TransactionRecord.class);
    assertThat(depositResp.getStatusCode()).isEqualTo(HttpStatus.OK);

    // build SQS client pointing to LocalStack
    SqsClient sqs = SqsClient.builder()
        .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.SQS))
        .region(software.amazon.awssdk.regions.Region.of(localstack.getRegion()))
        .credentialsProvider(localstack.getDefaultCredentialsProvider())
        .build();

    String queueUrl = localstack.getEndpointOverride(LocalStackContainer.Service.SQS).toString() + "/000000000000/banco-transactions";

    // wait a bit and then receive messages
    Thread.sleep(1000);

    ReceiveMessageResponse resp = sqs.receiveMessage(ReceiveMessageRequest.builder().queueUrl(queueUrl).maxNumberOfMessages(5).waitTimeSeconds(1).build());
    List<Message> messages = resp.messages();
    assertThat(messages).isNotEmpty();

    // cleanup: delete messages
    for (Message m : messages) {
      sqs.deleteMessage(DeleteMessageRequest.builder().queueUrl(queueUrl).receiptHandle(m.receiptHandle()).build());
    }
  }
}

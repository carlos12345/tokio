package com.example.bank.sqs;

import com.example.bank.model.TransactionRecord;
import com.example.bank.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqsPoller {

  private final SqsClient sqsClient;
  @Value("${aws.sqs.queue-url}")
  private String queueUrl;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final TransactionRepository transactionRepository;

  @Scheduled(fixedDelayString = "5000")
  public void poll() {
    var req = ReceiveMessageRequest.builder()
        .queueUrl(queueUrl)
        .maxNumberOfMessages(5)
        .waitTimeSeconds(5)
        .build();
    var resp = sqsClient.receiveMessage(req);
    resp.messages().forEach(msg -> {
      try {
        TransactionRecord tr = objectMapper.readValue(msg.body(), TransactionRecord.class);
        Optional<TransactionRecord> maybe = transactionRepository.findById(tr.getId());
        maybe.ifPresent(tx -> {
          tx.setStatus("PROCESSED_BY_SQS");
          transactionRepository.save(tx);
        });
        sqsClient.deleteMessage(DeleteMessageRequest.builder().queueUrl(queueUrl).receiptHandle(msg.receiptHandle()).build());
      } catch (Exception e) {
        log.error("Error processing message", e);
      }
    });
  }
}

package com.example.bank.sqs;

import com.example.bank.model.TransactionRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
@RequiredArgsConstructor
public class SqsSender {

  private final SqsClient sqsClient;
  @Value("${aws.sqs.queue-url}")
  private String queueUrl;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public void sendMessage(TransactionRecord tx) {
    try {
      String body = objectMapper.writeValueAsString(tx);
      SendMessageRequest req = SendMessageRequest.builder()
          .queueUrl(queueUrl)
          .messageBody(body)
          .build();
      sqsClient.sendMessage(req);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

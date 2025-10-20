package com.example.bank.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Configuration
public class SqsConfig {
  @Value("${aws.region}")
  private String awsRegion;

  @Value("${aws.sqs.endpoint:}")
  private String awsSqsEndpoint;

  @Bean
  public SqsClient sqsClient() {
    SqsClient.Builder builder = SqsClient.builder().region(Region.of(awsRegion));
    if (awsSqsEndpoint != null && !awsSqsEndpoint.isBlank()) {
      builder = builder.endpointOverride(URI.create(awsSqsEndpoint));
    }
    return builder.build();
  }
}

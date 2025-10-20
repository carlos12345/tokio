package com.example.bank;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.LocalStackContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTestBase {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static LocalStackContainer localstack = new LocalStackContainer("localstack/localstack:2.3")
            .withServices(LocalStackContainer.Service.SQS);

    @BeforeAll
    static void setupQueue() {
        SqsClient sqs = SqsClient.builder()
                .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.SQS))
                .region(Region.of(localstack.getRegion()))
                .credentialsProvider(localstack.getDefaultCredentialsProvider())
                .build();
        sqs.createQueue(CreateQueueRequest.builder().queueName("banco-transactions").build());
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("aws.region", localstack::getRegion);
        registry.add("aws.sqs.queue-url", () ->
                localstack.getEndpointOverride(LocalStackContainer.Service.SQS).toString()
                        + "/000000000000/banco-transactions");
        registry.add("aws.sqs.endpoint", () ->
                localstack.getEndpointOverride(LocalStackContainer.Service.SQS).toString());
    }
}

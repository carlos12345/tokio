package com.example.bank;

import com.example.bank.dto.AuthRequest;
import com.example.bank.dto.AuthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthIntegrationTest extends IntegrationTestBase {

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void deveRegistrarELogarUsuario() {
    AuthRequest req = new AuthRequest();
    req.setUsername("user1");
    req.setPassword("senha123");

    ResponseEntity<AuthResponse> registerResp = restTemplate.postForEntity("/api/auth/register", req, AuthResponse.class);
    assertThat(registerResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(registerResp.getBody().getToken()).isNotBlank();

    ResponseEntity<AuthResponse> loginResp = restTemplate.postForEntity("/api/auth/login", req, AuthResponse.class);
    assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(loginResp.getBody().getToken()).isNotBlank();
  }
}

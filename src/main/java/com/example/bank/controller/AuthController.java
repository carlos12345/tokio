package com.example.bank.controller;

import com.example.bank.dto.AuthRequest;
import com.example.bank.dto.AuthResponse;
import com.example.bank.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
    return ResponseEntity.ok(authService.login(req));
  }
}

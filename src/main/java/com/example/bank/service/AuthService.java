package com.example.bank.service;

import com.example.bank.dto.AuthRequest;
import com.example.bank.dto.AuthResponse;
import com.example.bank.model.AppUser;
import com.example.bank.repository.UserRepository;
import com.example.bank.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public AuthResponse register(AuthRequest req) {
    if (userRepository.existsByUsername(req.getUsername())) throw new RuntimeException("User exists");
    AppUser u = new AppUser();
    u.setUsername(req.getUsername());
    u.setPassword(passwordEncoder.encode(req.getPassword()));
    u.setRole("ROLE_USER");
    userRepository.save(u);
    UserDetails ud = org.springframework.security.core.userdetails.User.withUsername(u.getUsername()).password(u.getPassword()).authorities(u.getRole()).build();
    String token = jwtService.generateToken(ud);
    return new AuthResponse(token);
  }

  public AuthResponse login(AuthRequest req) {
    Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
    UserDetails ud = (UserDetails) auth.getPrincipal();
    String token = jwtService.generateToken(ud);
    return new AuthResponse(token);
  }
}

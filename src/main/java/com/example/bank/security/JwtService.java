package com.example.bank.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

  @Value("${security.jwt.secret}")
  private String jwtSecret;

  @Value("${security.jwt.expiration-ms}")
  private long jwtExpirationMs;

  public String generateToken(UserDetails userDetails) {
    var now = Instant.now();
    return Jwts.builder()
        .setSubject(userDetails.getUsername())
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plusMillis(jwtExpirationMs)))
        .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8))).build().parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException ex) {
      return false;
    }
  }

  public String getUsername(String token) {
    return Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8))).build()
        .parseClaimsJws(token).getBody().getSubject();
  }
}

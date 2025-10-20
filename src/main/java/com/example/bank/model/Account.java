package com.example.bank.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String number;

  @Column(nullable = false)
  private BigDecimal balance = BigDecimal.ZERO;

  @Column(nullable = false)
  private String owner;
}

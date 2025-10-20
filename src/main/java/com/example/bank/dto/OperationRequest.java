package com.example.bank.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Data
public class OperationRequest {
  @NotBlank
  private String accountNumber;
  @NotNull
  private BigDecimal amount;
}

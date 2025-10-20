package com.example.bank.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CreateAccountRequest {
  @NotBlank
  private String number;
  @NotBlank
  private String owner;
}

package br.com.academicbit.piersen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ContractChangeRequest(
        @NotBlank String position,
        @NotNull @Positive BigDecimal salary) {
}

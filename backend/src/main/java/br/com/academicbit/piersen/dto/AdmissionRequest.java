package br.com.academicbit.piersen.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AdmissionRequest(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 digitos") String cpf,
        @NotBlank @Email String email,
        @NotBlank String position,
        @NotBlank String department,
        @NotNull @Positive BigDecimal salary,
        @NotNull LocalDate admissionDate) {
}

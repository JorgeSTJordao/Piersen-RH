package br.com.academicbit.piersen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CertificateRequestInput(
        @NotNull LocalDate absenceDate,
        @NotNull @Positive Integer daysOff,
        @NotBlank String documentUrl) {
}

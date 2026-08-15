package br.com.academicbit.piersen.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record VacationRequestInput(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate) {
}

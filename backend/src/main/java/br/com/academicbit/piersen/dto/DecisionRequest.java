package br.com.academicbit.piersen.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DecisionRequest(
        @NotNull Boolean approved,
        @Size(max = 255) String note) {
}

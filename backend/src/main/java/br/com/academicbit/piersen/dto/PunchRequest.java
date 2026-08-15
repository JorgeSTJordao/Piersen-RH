package br.com.academicbit.piersen.dto;

import br.com.academicbit.piersen.domain.PunchType;
import jakarta.validation.constraints.NotNull;

public record PunchRequest(@NotNull PunchType type) {
}

package br.com.academicbit.piersen.dto;

import br.com.academicbit.piersen.domain.PunchStatus;
import br.com.academicbit.piersen.domain.PunchType;
import br.com.academicbit.piersen.domain.TimePunch;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PunchResponse(
        Long id,
        Long employeeId,
        LocalDateTime punchedAt,
        LocalDate referenceDay,
        PunchType type,
        PunchStatus status) {

    public static PunchResponse from(TimePunch punch) {
        return new PunchResponse(punch.getId(), punch.getEmployee().getId(), punch.getPunchedAt(),
                punch.getReferenceDay(), punch.getType(), punch.getStatus());
    }
}

package br.com.academicbit.piersen.dto;

import java.time.LocalDate;
import java.util.List;

public record TimeSheetResponse(
        Long employeeId,
        LocalDate referenceDay,
        List<PunchResponse> punches,
        long workedMinutes) {
}

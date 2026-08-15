package br.com.academicbit.piersen.dto;

import br.com.academicbit.piersen.domain.RequestStatus;
import br.com.academicbit.piersen.domain.VacationRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record VacationResponse(
        Long id,
        Long employeeId,
        String employeeName,
        LocalDate startDate,
        LocalDate endDate,
        Integer daysRequested,
        RequestStatus status,
        LocalDateTime requestedAt,
        LocalDateTime decidedAt,
        String decisionNote) {

    public static VacationResponse from(VacationRequest request) {
        return new VacationResponse(request.getId(), request.getEmployee().getId(), request.getEmployee().getName(),
                request.getStartDate(), request.getEndDate(), request.getDaysRequested(), request.getStatus(),
                request.getRequestedAt(), request.getDecidedAt(), request.getDecisionNote());
    }
}

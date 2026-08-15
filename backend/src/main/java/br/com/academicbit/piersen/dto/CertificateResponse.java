package br.com.academicbit.piersen.dto;

import br.com.academicbit.piersen.domain.MedicalCertificate;
import br.com.academicbit.piersen.domain.RequestStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CertificateResponse(
        Long id,
        Long employeeId,
        String employeeName,
        LocalDate absenceDate,
        Integer daysOff,
        String documentUrl,
        RequestStatus status,
        LocalDateTime submittedAt,
        LocalDateTime reviewedAt,
        String decisionNote) {

    public static CertificateResponse from(MedicalCertificate certificate) {
        return new CertificateResponse(certificate.getId(), certificate.getEmployee().getId(),
                certificate.getEmployee().getName(), certificate.getAbsenceDate(), certificate.getDaysOff(),
                certificate.getDocumentUrl(), certificate.getStatus(), certificate.getSubmittedAt(),
                certificate.getReviewedAt(), certificate.getDecisionNote());
    }
}

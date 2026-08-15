package br.com.academicbit.piersen.service;

import br.com.academicbit.piersen.domain.Employee;
import br.com.academicbit.piersen.domain.MedicalCertificate;
import br.com.academicbit.piersen.domain.PunchStatus;
import br.com.academicbit.piersen.domain.RequestStatus;
import br.com.academicbit.piersen.domain.TimePunch;
import br.com.academicbit.piersen.dto.CertificateRequestInput;
import br.com.academicbit.piersen.exception.BusinessException;
import br.com.academicbit.piersen.exception.NotFoundException;
import br.com.academicbit.piersen.repository.MedicalCertificateRepository;
import br.com.academicbit.piersen.repository.TimePunchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final MedicalCertificateRepository certificateRepository;
    private final TimePunchRepository timePunchRepository;
    private final EmployeeService employeeService;
    private final NotificationService notificationService;
    private final Clock clock;

    @Transactional
    public MedicalCertificate submit(Long employeeId, CertificateRequestInput input) {
        Employee employee = employeeService.requireActive(employeeId);
        if (input.absenceDate().isAfter(LocalDate.now(clock))) {
            throw new BusinessException("A data da ausencia nao pode ser futura");
        }
        return certificateRepository.save(MedicalCertificate.builder()
                .employee(employee)
                .absenceDate(input.absenceDate())
                .daysOff(input.daysOff())
                .documentUrl(input.documentUrl())
                .status(RequestStatus.PENDENTE)
                .submittedAt(LocalDateTime.now(clock))
                .build());
    }

    @Transactional
    public MedicalCertificate decide(Long certificateId, boolean approved, String note) {
        MedicalCertificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new NotFoundException("Atestado nao encontrado: " + certificateId));
        if (certificate.getStatus() != RequestStatus.PENDENTE) {
            throw new BusinessException("O atestado ja foi avaliado");
        }
        certificate.setStatus(approved ? RequestStatus.APROVADA : RequestStatus.RECUSADA);
        certificate.setReviewedAt(LocalDateTime.now(clock));
        certificate.setDecisionNote(note);
        if (approved) {
            waivePunches(certificate);
        }
        notificationService.notify(certificate.getEmployee(),
                "Seu atestado de " + certificate.getAbsenceDate() + " foi " + certificate.getStatus().name().toLowerCase() + ".");
        return certificateRepository.save(certificate);
    }

    private void waivePunches(MedicalCertificate certificate) {
        List<TimePunch> affected = new ArrayList<>();
        for (int offset = 0; offset < certificate.getDaysOff(); offset++) {
            affected.addAll(timePunchRepository.findByEmployeeIdAndReferenceDayOrderByPunchedAtAsc(
                    certificate.getEmployee().getId(), certificate.getAbsenceDate().plusDays(offset)));
        }
        for (TimePunch punch : affected) {
            punch.setStatus(PunchStatus.ABONADO);
        }
        timePunchRepository.saveAll(affected);
    }

    @Transactional(readOnly = true)
    public List<MedicalCertificate> listFor(Long employeeId) {
        return certificateRepository.findByEmployeeIdOrderBySubmittedAtDesc(employeeId);
    }

    @Transactional(readOnly = true)
    public List<MedicalCertificate> listPending() {
        return certificateRepository.findByStatusOrderBySubmittedAtAsc(RequestStatus.PENDENTE);
    }
}

package br.com.academicbit.piersen.service;

import br.com.academicbit.piersen.domain.Employee;
import br.com.academicbit.piersen.domain.RequestStatus;
import br.com.academicbit.piersen.domain.VacationRequest;
import br.com.academicbit.piersen.dto.VacationRequestInput;
import br.com.academicbit.piersen.exception.BusinessException;
import br.com.academicbit.piersen.exception.NotFoundException;
import br.com.academicbit.piersen.repository.EmployeeRepository;
import br.com.academicbit.piersen.repository.VacationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VacationService {

    private static final int MINIMUM_DAYS = 5;
    private static final int MINIMUM_NOTICE_DAYS = 15;

    private final VacationRequestRepository vacationRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;
    private final NotificationService notificationService;
    private final Clock clock;

    @Transactional
    public VacationRequest request(Long employeeId, VacationRequestInput input) {
        Employee employee = employeeService.requireActive(employeeId);
        LocalDate today = LocalDate.now(clock);
        if (!input.endDate().isAfter(input.startDate())) {
            throw new BusinessException("A data final deve ser posterior a data inicial");
        }
        if (input.startDate().isBefore(today.plusDays(MINIMUM_NOTICE_DAYS))) {
            throw new BusinessException("A solicitacao deve ser feita com pelo menos " + MINIMUM_NOTICE_DAYS + " dias de antecedencia");
        }
        int days = (int) ChronoUnit.DAYS.between(input.startDate(), input.endDate()) + 1;
        if (days < MINIMUM_DAYS) {
            throw new BusinessException("O periodo minimo de ferias e de " + MINIMUM_DAYS + " dias");
        }
        if (days > employee.getVacationBalanceDays()) {
            throw new BusinessException("Saldo de ferias insuficiente: disponivel " + employee.getVacationBalanceDays() + " dias");
        }
        if (!overlapping(employeeId, input.startDate(), input.endDate()).isEmpty()) {
            throw new BusinessException("Ja existe solicitacao de ferias para o periodo informado");
        }
        return vacationRequestRepository.save(VacationRequest.builder()
                .employee(employee)
                .startDate(input.startDate())
                .endDate(input.endDate())
                .daysRequested(days)
                .status(RequestStatus.PENDENTE)
                .requestedAt(LocalDateTime.now(clock))
                .build());
    }

    private List<VacationRequest> overlapping(Long employeeId, LocalDate start, LocalDate end) {
        return vacationRequestRepository
                .findByEmployeeIdAndStatusInAndEndDateGreaterThanEqualAndStartDateLessThanEqual(
                        employeeId, List.of(RequestStatus.PENDENTE, RequestStatus.APROVADA), start, end);
    }

    @Transactional
    public VacationRequest decide(Long requestId, boolean approved, String note) {
        VacationRequest request = vacationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Solicitacao de ferias nao encontrada: " + requestId));
        if (request.getStatus() != RequestStatus.PENDENTE) {
            throw new BusinessException("A solicitacao ja foi avaliada");
        }
        request.setStatus(approved ? RequestStatus.APROVADA : RequestStatus.RECUSADA);
        request.setDecidedAt(LocalDateTime.now(clock));
        request.setDecisionNote(note);
        if (approved) {
            Employee employee = request.getEmployee();
            employee.setVacationBalanceDays(employee.getVacationBalanceDays() - request.getDaysRequested());
            employeeRepository.save(employee);
        }
        notificationService.notify(request.getEmployee(),
                "Sua solicitacao de ferias foi " + request.getStatus().name().toLowerCase() + ".");
        return vacationRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<VacationRequest> listFor(Long employeeId) {
        return vacationRequestRepository.findByEmployeeIdOrderByRequestedAtDesc(employeeId);
    }

    @Transactional(readOnly = true)
    public List<VacationRequest> listPending() {
        return vacationRequestRepository.findByStatusOrderByRequestedAtAsc(RequestStatus.PENDENTE);
    }
}

package br.com.academicbit.piersen.service;

import br.com.academicbit.piersen.TestFixtures;
import br.com.academicbit.piersen.domain.Employee;
import br.com.academicbit.piersen.domain.RequestStatus;
import br.com.academicbit.piersen.domain.VacationRequest;
import br.com.academicbit.piersen.dto.VacationRequestInput;
import br.com.academicbit.piersen.exception.BusinessException;
import br.com.academicbit.piersen.repository.EmployeeRepository;
import br.com.academicbit.piersen.repository.VacationRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("VacationService - solicitacao e aprovacao de ferias")
class VacationServiceTest {

    @Mock
    private VacationRequestRepository vacationRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private NotificationService notificationService;

    private VacationService vacationService;

    private final Clock clock = TestFixtures.fixedClock();
    private final Employee employee = TestFixtures.activeEmployee(1L);

    @BeforeEach
    void setUp() {
        vacationService = new VacationService(vacationRequestRepository, employeeRepository, employeeService,
                notificationService, clock);
        when(employeeService.requireActive(1L)).thenReturn(employee);
        when(vacationRequestRepository.save(any(VacationRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(vacationRequestRepository
                .findByEmployeeIdAndStatusInAndEndDateGreaterThanEqualAndStartDateLessThanEqual(
                        anyLong(), anyList(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());
    }

    private VacationRequestInput input(LocalDate start, LocalDate end) {
        return new VacationRequestInput(start, end);
    }

    private VacationRequest pendingRequest(int days) {
        return VacationRequest.builder()
                .id(7L)
                .employee(employee)
                .startDate(TestFixtures.TODAY.plusDays(30))
                .endDate(TestFixtures.TODAY.plusDays(30 + days - 1))
                .daysRequested(days)
                .status(RequestStatus.PENDENTE)
                .requestedAt(LocalDateTime.now(clock))
                .build();
    }

    @Test
    @DisplayName("cria solicitacao de ferias com status PENDENTE para aprovacao do RH")
    void shouldCreatePendingRequest() {
        VacationRequest request = vacationService.request(1L,
                input(TestFixtures.TODAY.plusDays(30), TestFixtures.TODAY.plusDays(39)));
        assertThat(request.getStatus()).isEqualTo(RequestStatus.PENDENTE);
        assertThat(request.getDaysRequested()).isEqualTo(10);
    }

    @Test
    @DisplayName("recusa periodo com data final anterior a inicial")
    void shouldRejectInvertedPeriod() {
        assertThatThrownBy(() -> vacationService.request(1L,
                input(TestFixtures.TODAY.plusDays(40), TestFixtures.TODAY.plusDays(30))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("posterior");
    }

    @Test
    @DisplayName("exige antecedencia minima de 15 dias")
    void shouldRequireMinimumNotice() {
        assertThatThrownBy(() -> vacationService.request(1L,
                input(TestFixtures.TODAY.plusDays(3), TestFixtures.TODAY.plusDays(13))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("antecedencia");
    }

    @Test
    @DisplayName("exige periodo minimo de 5 dias")
    void shouldRequireMinimumPeriod() {
        assertThatThrownBy(() -> vacationService.request(1L,
                input(TestFixtures.TODAY.plusDays(30), TestFixtures.TODAY.plusDays(32))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("periodo minimo");
    }

    @Test
    @DisplayName("recusa solicitacao acima do saldo disponivel")
    void shouldRejectRequestAboveBalance() {
        employee.setVacationBalanceDays(5);
        assertThatThrownBy(() -> vacationService.request(1L,
                input(TestFixtures.TODAY.plusDays(30), TestFixtures.TODAY.plusDays(49))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Saldo de ferias insuficiente");
    }

    @Test
    @DisplayName("recusa periodo sobreposto a outra solicitacao")
    void shouldRejectOverlappingPeriod() {
        when(vacationRequestRepository
                .findByEmployeeIdAndStatusInAndEndDateGreaterThanEqualAndStartDateLessThanEqual(
                        anyLong(), anyList(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(pendingRequest(10)));
        assertThatThrownBy(() -> vacationService.request(1L,
                input(TestFixtures.TODAY.plusDays(30), TestFixtures.TODAY.plusDays(39))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ja existe solicitacao");
    }

    @Test
    @DisplayName("aprovacao do RH debita o saldo e notifica o funcionario")
    void shouldApproveAndDebitBalance() {
        when(vacationRequestRepository.findById(7L)).thenReturn(Optional.of(pendingRequest(10)));
        VacationRequest decided = vacationService.decide(7L, true, "Aprovado pelo gestor");
        assertThat(decided.getStatus()).isEqualTo(RequestStatus.APROVADA);
        assertThat(employee.getVacationBalanceDays()).isEqualTo(20);
        verify(notificationService).notify(any(Employee.class), anyString());
    }

    @Test
    @DisplayName("recusa do RH mantem o saldo de ferias")
    void shouldRejectWithoutDebitingBalance() {
        when(vacationRequestRepository.findById(7L)).thenReturn(Optional.of(pendingRequest(10)));
        VacationRequest decided = vacationService.decide(7L, false, "Periodo critico");
        assertThat(decided.getStatus()).isEqualTo(RequestStatus.RECUSADA);
        assertThat(decided.getDecisionNote()).isEqualTo("Periodo critico");
        assertThat(employee.getVacationBalanceDays()).isEqualTo(30);
    }

    @Test
    @DisplayName("nao permite avaliar duas vezes a mesma solicitacao")
    void shouldRejectDoubleDecision() {
        VacationRequest alreadyDecided = pendingRequest(10);
        alreadyDecided.setStatus(RequestStatus.APROVADA);
        when(vacationRequestRepository.findById(7L)).thenReturn(Optional.of(alreadyDecided));
        assertThatThrownBy(() -> vacationService.decide(7L, false, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ja foi avaliada");
    }
}

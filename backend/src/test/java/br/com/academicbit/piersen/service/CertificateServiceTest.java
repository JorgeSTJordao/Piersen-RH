package br.com.academicbit.piersen.service;

import br.com.academicbit.piersen.TestFixtures;
import br.com.academicbit.piersen.domain.Employee;
import br.com.academicbit.piersen.domain.MedicalCertificate;
import br.com.academicbit.piersen.domain.PunchStatus;
import br.com.academicbit.piersen.domain.PunchType;
import br.com.academicbit.piersen.domain.RequestStatus;
import br.com.academicbit.piersen.domain.TimePunch;
import br.com.academicbit.piersen.dto.CertificateRequestInput;
import br.com.academicbit.piersen.exception.BusinessException;
import br.com.academicbit.piersen.repository.MedicalCertificateRepository;
import br.com.academicbit.piersen.repository.TimePunchRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CertificateService - atestados e abono de horas")
class CertificateServiceTest {

    @Mock
    private MedicalCertificateRepository certificateRepository;

    @Mock
    private TimePunchRepository timePunchRepository;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private NotificationService notificationService;

    private CertificateService certificateService;

    private final Clock clock = TestFixtures.fixedClock();
    private final Employee employee = TestFixtures.activeEmployee(1L);

    @BeforeEach
    void setUp() {
        certificateService = new CertificateService(certificateRepository, timePunchRepository, employeeService,
                notificationService, clock);
        when(employeeService.requireActive(1L)).thenReturn(employee);
        when(certificateRepository.save(any(MedicalCertificate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(timePunchRepository.findByEmployeeIdAndReferenceDayOrderByPunchedAtAsc(anyLong(), any(LocalDate.class)))
                .thenReturn(List.of());
    }

    private MedicalCertificate pendingCertificate(int daysOff) {
        return MedicalCertificate.builder()
                .id(3L)
                .employee(employee)
                .absenceDate(TestFixtures.TODAY.minusDays(1))
                .daysOff(daysOff)
                .documentUrl("https://cdn/atestado.png")
                .status(RequestStatus.PENDENTE)
                .submittedAt(LocalDateTime.now(clock))
                .build();
    }

    @Test
    @DisplayName("registra atestado com status PENDENTE para analise do RH")
    void shouldSubmitPendingCertificate() {
        MedicalCertificate certificate = certificateService.submit(1L, new CertificateRequestInput(
                TestFixtures.TODAY.minusDays(1), 2, "https://cdn/atestado.png"));
        assertThat(certificate.getStatus()).isEqualTo(RequestStatus.PENDENTE);
        assertThat(certificate.getDaysOff()).isEqualTo(2);
    }

    @Test
    @DisplayName("recusa atestado com data futura")
    void shouldRejectFutureAbsenceDate() {
        assertThatThrownBy(() -> certificateService.submit(1L, new CertificateRequestInput(
                TestFixtures.TODAY.plusDays(1), 1, "https://cdn/atestado.png")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("futura");
    }

    @Test
    @DisplayName("aprovacao do RH abona as marcacoes dos dias cobertos pelo atestado")
    void shouldWaivePunchesOnApproval() {
        TimePunch punch = TimePunch.builder()
                .employee(employee)
                .type(PunchType.ENTRADA)
                .punchedAt(LocalDateTime.of(TestFixtures.TODAY.minusDays(1), java.time.LocalTime.of(8, 0)))
                .referenceDay(TestFixtures.TODAY.minusDays(1))
                .status(PunchStatus.REGISTRADO)
                .build();
        when(certificateRepository.findById(3L)).thenReturn(Optional.of(pendingCertificate(1)));
        when(timePunchRepository.findByEmployeeIdAndReferenceDayOrderByPunchedAtAsc(1L, TestFixtures.TODAY.minusDays(1)))
                .thenReturn(List.of(punch));
        MedicalCertificate decided = certificateService.decide(3L, true, "Abonado");
        assertThat(decided.getStatus()).isEqualTo(RequestStatus.APROVADA);
        assertThat(punch.getStatus()).isEqualTo(PunchStatus.ABONADO);
        verify(timePunchRepository).saveAll(List.of(punch));
        verify(notificationService).notify(any(Employee.class), anyString());
    }

    @Test
    @DisplayName("recusa do RH nao abona marcacoes")
    void shouldNotWaivePunchesOnRejection() {
        when(certificateRepository.findById(3L)).thenReturn(Optional.of(pendingCertificate(1)));
        MedicalCertificate decided = certificateService.decide(3L, false, "Documento ilegivel");
        assertThat(decided.getStatus()).isEqualTo(RequestStatus.RECUSADA);
        verify(timePunchRepository, org.mockito.Mockito.never()).saveAll(any());
    }

    @Test
    @DisplayName("nao permite avaliar duas vezes o mesmo atestado")
    void shouldRejectDoubleDecision() {
        MedicalCertificate certificate = pendingCertificate(1);
        certificate.setStatus(RequestStatus.APROVADA);
        when(certificateRepository.findById(3L)).thenReturn(Optional.of(certificate));
        assertThatThrownBy(() -> certificateService.decide(3L, true, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ja foi avaliado");
    }
}

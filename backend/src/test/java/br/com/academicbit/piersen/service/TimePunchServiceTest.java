package br.com.academicbit.piersen.service;

import br.com.academicbit.piersen.TestFixtures;
import br.com.academicbit.piersen.domain.Employee;
import br.com.academicbit.piersen.domain.PunchStatus;
import br.com.academicbit.piersen.domain.PunchType;
import br.com.academicbit.piersen.domain.TimePunch;
import br.com.academicbit.piersen.dto.TimeSheetResponse;
import br.com.academicbit.piersen.exception.AccessDeniedBusinessException;
import br.com.academicbit.piersen.exception.BusinessException;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TimePunchService - marcacao de ponto e espelho")
class TimePunchServiceTest {

    @Mock
    private TimePunchRepository timePunchRepository;

    @Mock
    private EmployeeService employeeService;

    private TimePunchService timePunchService;

    private final Clock clock = TestFixtures.fixedClock();
    private final Employee employee = TestFixtures.activeEmployee(1L);

    @BeforeEach
    void setUp() {
        timePunchService = new TimePunchService(timePunchRepository, employeeService, clock);
        when(employeeService.requireActive(1L)).thenReturn(employee);
        when(timePunchRepository.save(any(TimePunch.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private TimePunch punchAt(PunchType type, LocalTime time) {
        return TimePunch.builder()
                .employee(employee)
                .type(type)
                .punchedAt(LocalDateTime.of(TestFixtures.TODAY, time))
                .referenceDay(TestFixtures.TODAY)
                .status(PunchStatus.REGISTRADO)
                .build();
    }

    private void givenTodayPunches(List<TimePunch> punches) {
        when(timePunchRepository.findByEmployeeIdAndReferenceDayOrderByPunchedAtAsc(1L, TestFixtures.TODAY))
                .thenReturn(punches);
    }

    @Test
    @DisplayName("registra a entrada do dia com status REGISTRADO para conferencia do RH")
    void shouldRegisterEntry() {
        givenTodayPunches(List.of());
        TimePunch punch = timePunchService.punch(1L, PunchType.ENTRADA);
        assertThat(punch.getType()).isEqualTo(PunchType.ENTRADA);
        assertThat(punch.getStatus()).isEqualTo(PunchStatus.REGISTRADO);
        assertThat(punch.getReferenceDay()).isEqualTo(TestFixtures.TODAY);
    }

    @Test
    @DisplayName("bloqueia marcacao de funcionario desligado")
    void shouldBlockTerminatedEmployee() {
        when(employeeService.requireActive(2L))
                .thenThrow(new AccessDeniedBusinessException("Funcionario desligado nao possui acesso ao portal"));
        assertThatThrownBy(() -> timePunchService.punch(2L, PunchType.ENTRADA))
                .isInstanceOf(AccessDeniedBusinessException.class);
    }

    @Test
    @DisplayName("recusa duas entradas seguidas no mesmo dia")
    void shouldRejectDoubleEntry() {
        givenTodayPunches(List.of(punchAt(PunchType.ENTRADA, LocalTime.of(8, 0))));
        assertThatThrownBy(() -> timePunchService.punch(1L, PunchType.ENTRADA))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("jornada aberta");
    }

    @Test
    @DisplayName("recusa saida sem entrada registrada")
    void shouldRejectExitWithoutEntry() {
        givenTodayPunches(List.of());
        assertThatThrownBy(() -> timePunchService.punch(1L, PunchType.SAIDA))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Registre a entrada");
    }

    @Test
    @DisplayName("recusa fim de intervalo sem inicio de intervalo")
    void shouldRejectBreakEndWithoutBreakStart() {
        givenTodayPunches(List.of(punchAt(PunchType.ENTRADA, LocalTime.of(8, 0))));
        assertThatThrownBy(() -> timePunchService.punch(1L, PunchType.FIM_INTERVALO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Sequencia de marcacao invalida");
    }

    @Test
    @DisplayName("aceita saida apos o fim do intervalo")
    void shouldAcceptExitAfterBreakEnd() {
        givenTodayPunches(List.of(punchAt(PunchType.ENTRADA, LocalTime.of(8, 0)),
                punchAt(PunchType.INICIO_INTERVALO, LocalTime.of(12, 0)),
                punchAt(PunchType.FIM_INTERVALO, LocalTime.of(13, 0))));
        assertThat(timePunchService.punch(1L, PunchType.SAIDA).getType()).isEqualTo(PunchType.SAIDA);
    }

    @Test
    @DisplayName("calcula as horas trabalhadas descontando o intervalo")
    void shouldCalculateWorkedMinutes() {
        List<TimePunch> punches = List.of(punchAt(PunchType.ENTRADA, LocalTime.of(8, 0)),
                punchAt(PunchType.INICIO_INTERVALO, LocalTime.of(12, 0)),
                punchAt(PunchType.FIM_INTERVALO, LocalTime.of(13, 0)),
                punchAt(PunchType.SAIDA, LocalTime.of(17, 0)));
        assertThat(timePunchService.workedMinutes(punches)).isEqualTo(480);
    }

    @Test
    @DisplayName("monta o espelho de ponto do dia")
    void shouldBuildTimeSheet() {
        givenTodayPunches(List.of(punchAt(PunchType.ENTRADA, LocalTime.of(9, 0)),
                punchAt(PunchType.SAIDA, LocalTime.of(18, 0))));
        TimeSheetResponse sheet = timePunchService.timeSheet(1L, TestFixtures.TODAY);
        assertThat(sheet.punches()).hasSize(2);
        assertThat(sheet.workedMinutes()).isEqualTo(540);
    }

    @Test
    @DisplayName("RH confere a marcacao alterando o status")
    void shouldReviewPunch() {
        TimePunch punch = punchAt(PunchType.ENTRADA, LocalTime.of(8, 0));
        when(timePunchRepository.findById(anyLong())).thenReturn(Optional.of(punch));
        assertThat(timePunchService.review(5L, PunchStatus.CONFERIDO).getStatus()).isEqualTo(PunchStatus.CONFERIDO);
    }

    @Test
    @DisplayName("conferencia nao pode devolver o status REGISTRADO")
    void shouldRejectInvalidReviewStatus() {
        when(timePunchRepository.findById(anyLong()))
                .thenReturn(Optional.of(punchAt(PunchType.ENTRADA, LocalTime.of(8, 0))));
        assertThatThrownBy(() -> timePunchService.review(5L, PunchStatus.REGISTRADO))
                .isInstanceOf(BusinessException.class);
    }
}

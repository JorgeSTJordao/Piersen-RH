package br.com.academicbit.piersen.service;

import br.com.academicbit.piersen.TestFixtures;
import br.com.academicbit.piersen.domain.Payslip;
import br.com.academicbit.piersen.exception.BusinessException;
import br.com.academicbit.piersen.repository.PayslipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PayslipService - geracao e consulta de holerite")
class PayslipServiceTest {

    @Mock
    private PayslipRepository payslipRepository;

    @Mock
    private EmployeeService employeeService;

    private PayslipService payslipService;

    @BeforeEach
    void setUp() {
        payslipService = new PayslipService(payslipRepository, employeeService);
        when(employeeService.findById(1L)).thenReturn(TestFixtures.activeEmployee(1L));
        when(payslipRepository.save(any(Payslip.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(payslipRepository.findByEmployeeIdAndReferenceMonth(anyLong(), anyString())).thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("gera holerite com INSS e IRRF descontados do salario bruto")
    void shouldGeneratePayslipWithDeductions() {
        Payslip payslip = payslipService.generate(1L, YearMonth.of(2026, 7));
        assertThat(payslip.getGrossSalary()).isEqualByComparingTo("6000.00");
        assertThat(payslip.getDeductions()).isEqualByComparingTo("1060.50");
        assertThat(payslip.getNetSalary()).isEqualByComparingTo("4939.50");
        assertThat(payslip.getReferenceMonth()).isEqualTo("2026-07");
    }

    @Test
    @DisplayName("isenta de IRRF a base abaixo do limite")
    void shouldExemptIrrfBelowThreshold() {
        assertThat(payslipService.deductionsFor(new BigDecimal("2000.00"))).isEqualByComparingTo("220.00");
    }

    @Test
    @DisplayName("nao gera holerite duplicado para a mesma competencia")
    void shouldRejectDuplicatedReference() {
        when(payslipRepository.findByEmployeeIdAndReferenceMonth(1L, "2026-07"))
                .thenReturn(Optional.of(Payslip.builder().build()));
        assertThatThrownBy(() -> payslipService.generate(1L, YearMonth.of(2026, 7)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ja gerado");
    }

    @Test
    @DisplayName("nao gera holerite para competencia anterior a admissao")
    void shouldRejectReferenceBeforeAdmission() {
        assertThatThrownBy(() -> payslipService.generate(1L, YearMonth.of(2020, 1)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("anterior a admissao");
    }
}

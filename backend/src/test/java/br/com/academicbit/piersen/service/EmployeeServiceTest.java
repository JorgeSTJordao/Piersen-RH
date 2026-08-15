package br.com.academicbit.piersen.service;

import br.com.academicbit.piersen.TestFixtures;
import br.com.academicbit.piersen.domain.ContractChange;
import br.com.academicbit.piersen.domain.Employee;
import br.com.academicbit.piersen.domain.EmployeeStatus;
import br.com.academicbit.piersen.domain.Role;
import br.com.academicbit.piersen.dto.AdmissionRequest;
import br.com.academicbit.piersen.dto.ContractChangeRequest;
import br.com.academicbit.piersen.dto.PersonalDataRequest;
import br.com.academicbit.piersen.exception.AccessDeniedBusinessException;
import br.com.academicbit.piersen.exception.BusinessException;
import br.com.academicbit.piersen.exception.NotFoundException;
import br.com.academicbit.piersen.repository.ContractChangeRepository;
import br.com.academicbit.piersen.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("EmployeeService - admissao, desligamento e alteracao contratual")
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ContractChangeRepository contractChangeRepository;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CredentialMailer credentialMailer;

    @Mock
    private NotificationService notificationService;

    private EmployeeService employeeService;

    private final Clock clock = TestFixtures.fixedClock();

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(employeeRepository, contractChangeRepository, passwordGenerator,
                passwordEncoder, credentialMailer, notificationService, clock);
        when(passwordGenerator.generate()).thenReturn("SenhaGerada1");
        when(passwordEncoder.encode(anyString())).thenReturn("hash-bcrypt");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private AdmissionRequest admissionRequest() {
        return new AdmissionRequest("Maria Souza", "12345678901", "maria.souza@piersen.com.br",
                "Desenvolvedora", "Tecnologia", new BigDecimal("6000.00"), TestFixtures.TODAY.minusDays(1));
    }

    @Test
    @DisplayName("admite funcionario com status ATIVO, papel FUNCIONARIO e saldo inicial de ferias")
    void shouldAdmitEmployeeAsActive() {
        Employee admitted = employeeService.admit(admissionRequest());
        assertThat(admitted.getStatus()).isEqualTo(EmployeeStatus.ATIVO);
        assertThat(admitted.getRole()).isEqualTo(Role.FUNCIONARIO);
        assertThat(admitted.getVacationBalanceDays()).isEqualTo(30);
        assertThat(admitted.getPasswordHash()).isEqualTo("hash-bcrypt");
    }

    @Test
    @DisplayName("envia as credenciais geradas por e-mail apos a admissao")
    void shouldSendGeneratedCredentials() {
        Employee admitted = employeeService.admit(admissionRequest());
        verify(credentialMailer).sendCredentials(admitted, "SenhaGerada1");
        verify(notificationService).notify(eq(admitted), anyString());
    }

    @Test
    @DisplayName("recusa admissao com CPF ja cadastrado")
    void shouldRejectDuplicatedCpf() {
        when(employeeRepository.existsByCpf("12345678901")).thenReturn(true);
        assertThatThrownBy(() -> employeeService.admit(admissionRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CPF");
    }

    @Test
    @DisplayName("recusa admissao com e-mail ja cadastrado")
    void shouldRejectDuplicatedEmail() {
        when(employeeRepository.existsByEmailIgnoreCase("maria.souza@piersen.com.br")).thenReturn(true);
        assertThatThrownBy(() -> employeeService.admit(admissionRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("e-mail");
    }

    @Test
    @DisplayName("recusa admissao com data futura")
    void shouldRejectFutureAdmissionDate() {
        AdmissionRequest request = new AdmissionRequest("Joao", "99999999999", "joao@piersen.com.br",
                "Analista", "Financeiro", new BigDecimal("4000.00"), TestFixtures.TODAY.plusDays(1));
        assertThatThrownBy(() -> employeeService.admit(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("futura");
    }

    @Test
    @DisplayName("desliga funcionario preservando o historico (soft delete)")
    void shouldTerminateAsSoftDelete() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(TestFixtures.activeEmployee(1L)));
        Employee terminated = employeeService.terminate(1L, TestFixtures.TODAY);
        assertThat(terminated.getStatus()).isEqualTo(EmployeeStatus.DESLIGADO);
        assertThat(terminated.getTerminationDate()).isEqualTo(TestFixtures.TODAY);
        verify(employeeRepository, never()).delete(any(Employee.class));
    }

    @Test
    @DisplayName("nao permite desligar funcionario ja desligado")
    void shouldRejectDoubleTermination() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(TestFixtures.terminatedEmployee(1L)));
        assertThatThrownBy(() -> employeeService.terminate(1L, TestFixtures.TODAY))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ja esta desligado");
    }

    @Test
    @DisplayName("nao permite data de desligamento anterior a admissao")
    void shouldRejectTerminationBeforeAdmission() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(TestFixtures.activeEmployee(1L)));
        assertThatThrownBy(() -> employeeService.terminate(1L, TestFixtures.TODAY.minusYears(5)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("anterior a admissao");
    }

    @Test
    @DisplayName("funcionario atualiza apenas dados basicos, sem alterar salario, cargo ou departamento")
    void shouldUpdateOnlyPersonalData() {
        Employee employee = TestFixtures.activeEmployee(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        Employee updated = employeeService.updatePersonalData(1L,
                new PersonalDataRequest("11999998888", "Rua das Flores, 100", "https://cdn/foto.png"));
        assertThat(updated.getPhone()).isEqualTo("11999998888");
        assertThat(updated.getAddress()).isEqualTo("Rua das Flores, 100");
        assertThat(updated.getPhotoUrl()).isEqualTo("https://cdn/foto.png");
        assertThat(updated.getSalary()).isEqualByComparingTo("6000.00");
        assertThat(updated.getPosition()).isEqualTo("Desenvolvedora");
        assertThat(updated.getDepartment()).isEqualTo("Tecnologia");
    }

    @Test
    @DisplayName("bloqueia atualizacao de dados de funcionario desligado")
    void shouldRejectPersonalDataUpdateForTerminated() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(TestFixtures.terminatedEmployee(1L)));
        assertThatThrownBy(() -> employeeService.updatePersonalData(1L, new PersonalDataRequest("119", null, null)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("apenas o RH altera cargo e salario")
    void shouldRejectContractChangeByEmployee() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(TestFixtures.activeEmployee(2L)));
        assertThatThrownBy(() -> employeeService.changeContract(2L, 1L,
                new ContractChangeRequest("Gerente", new BigDecimal("15000.00"))))
                .isInstanceOf(AccessDeniedBusinessException.class)
                .hasMessageContaining("Apenas o RH");
    }

    @Test
    @DisplayName("RH altera cargo e salario registrando o historico contratual")
    void shouldChangeContractAndKeepHistory() {
        when(employeeRepository.findById(9L)).thenReturn(Optional.of(TestFixtures.hrUser(9L)));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(TestFixtures.activeEmployee(1L)));
        Employee updated = employeeService.changeContract(9L, 1L,
                new ContractChangeRequest("Tech Lead", new BigDecimal("12000.00")));
        ArgumentCaptor<ContractChange> captor = ArgumentCaptor.forClass(ContractChange.class);
        verify(contractChangeRepository).save(captor.capture());
        assertThat(updated.getPosition()).isEqualTo("Tech Lead");
        assertThat(updated.getSalary()).isEqualByComparingTo("12000.00");
        assertThat(captor.getValue().getPreviousPosition()).isEqualTo("Desenvolvedora");
        assertThat(captor.getValue().getPreviousSalary()).isEqualByComparingTo("6000.00");
    }

    @Test
    @DisplayName("bloqueia acesso ao portal de funcionario desligado")
    void shouldBlockPortalAccessForTerminated() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(TestFixtures.terminatedEmployee(1L)));
        assertThatThrownBy(() -> employeeService.requireActive(1L))
                .isInstanceOf(AccessDeniedBusinessException.class)
                .hasMessageContaining("desligado");
    }

    @Test
    @DisplayName("lanca NotFound para funcionario inexistente")
    void shouldThrowNotFound() {
        when(employeeRepository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> employeeService.findById(404L)).isInstanceOf(NotFoundException.class);
    }
}

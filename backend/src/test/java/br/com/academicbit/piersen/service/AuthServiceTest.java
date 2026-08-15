package br.com.academicbit.piersen.service;

import br.com.academicbit.piersen.TestFixtures;
import br.com.academicbit.piersen.domain.Employee;
import br.com.academicbit.piersen.domain.Role;
import br.com.academicbit.piersen.dto.LoginRequest;
import br.com.academicbit.piersen.dto.LoginResponse;
import br.com.academicbit.piersen.exception.AccessDeniedBusinessException;
import br.com.academicbit.piersen.repository.EmployeeRepository;
import br.com.academicbit.piersen.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AuthService - login e revogacao de acesso")
class AuthServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(employeeRepository, passwordEncoder, jwtService);
        when(jwtService.generate(any(Employee.class))).thenReturn("jwt-token");
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    }

    @Test
    @DisplayName("autentica funcionario ativo e devolve token com o papel")
    void shouldAuthenticateActiveEmployee() {
        when(employeeRepository.findByEmailIgnoreCase("maria.souza@piersen.com.br"))
                .thenReturn(Optional.of(TestFixtures.activeEmployee(1L)));
        LoginResponse response = authService.login(new LoginRequest("maria.souza@piersen.com.br", "senha"));
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.employeeId()).isEqualTo(1L);
        assertThat(response.role()).isEqualTo(Role.FUNCIONARIO);
    }

    @Test
    @DisplayName("bloqueia login de funcionario desligado")
    void shouldBlockTerminatedEmployeeLogin() {
        when(employeeRepository.findByEmailIgnoreCase("maria.souza@piersen.com.br"))
                .thenReturn(Optional.of(TestFixtures.terminatedEmployee(1L)));
        assertThatThrownBy(() -> authService.login(new LoginRequest("maria.souza@piersen.com.br", "senha")))
                .isInstanceOf(AccessDeniedBusinessException.class)
                .hasMessageContaining("desligado");
    }

    @Test
    @DisplayName("recusa senha invalida")
    void shouldRejectWrongPassword() {
        when(employeeRepository.findByEmailIgnoreCase("maria.souza@piersen.com.br"))
                .thenReturn(Optional.of(TestFixtures.activeEmployee(1L)));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        assertThatThrownBy(() -> authService.login(new LoginRequest("maria.souza@piersen.com.br", "errada")))
                .isInstanceOf(AccessDeniedBusinessException.class)
                .hasMessageContaining("Credenciais invalidas");
    }

    @Test
    @DisplayName("recusa e-mail nao cadastrado")
    void shouldRejectUnknownEmail() {
        when(employeeRepository.findByEmailIgnoreCase("ninguem@piersen.com.br")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.login(new LoginRequest("ninguem@piersen.com.br", "senha")))
                .isInstanceOf(AccessDeniedBusinessException.class);
    }
}

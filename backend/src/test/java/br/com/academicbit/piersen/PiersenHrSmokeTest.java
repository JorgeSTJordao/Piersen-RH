package br.com.academicbit.piersen;

import br.com.academicbit.piersen.dto.EmployeeResponse;
import br.com.academicbit.piersen.dto.LoginRequest;
import br.com.academicbit.piersen.dto.LoginResponse;
import br.com.academicbit.piersen.dto.PunchRequest;
import br.com.academicbit.piersen.domain.PunchType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Piersen HR - verificacao de ponta a ponta da aplicacao")
class PiersenHrSmokeTest {

    private static final String DEMO_PASSWORD = "Piersen@2026";

    @Autowired
    private TestRestTemplate restTemplate;

    private LoginResponse login(String email) {
        return restTemplate.postForEntity("/api/auth/login", new LoginRequest(email, DEMO_PASSWORD),
                LoginResponse.class).getBody();
    }

    private HttpEntity<Object> authorized(String token, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }

    @Test
    @DisplayName("carrega a base demo e autentica o RH")
    void shouldLoadDemoDataAndAuthenticateHr() {
        LoginResponse hr = login("carla.menezes.rh@gmail.com");
        assertThat(hr).isNotNull();
        assertThat(hr.role().name()).isEqualTo("RH");
        ResponseEntity<EmployeeResponse[]> employees = restTemplate.exchange("/api/employees", HttpMethod.GET,
                authorized(hr.token(), null), EmployeeResponse[].class);
        assertThat(employees.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(employees.getBody()).hasSize(7);
    }

    @Test
    @DisplayName("bloqueia o login do funcionario desligado")
    void shouldBlockTerminatedEmployee() {
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login",
                new LoginRequest("larissa.fontes@bol.com.br", DEMO_PASSWORD), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("desligado");
    }

    @Test
    @DisplayName("impede o funcionario de acessar rotas exclusivas do RH")
    void shouldForbidEmployeeOnHrRoutes() {
        LoginResponse employee = login("thiago.barbosa@uol.com.br");
        ResponseEntity<String> response = restTemplate.exchange("/api/employees", HttpMethod.GET,
                authorized(employee.token(), null), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("funcionario registra ponto e consulta o proprio espelho")
    void shouldPunchAndReadTimeSheet() {
        LoginResponse employee = login("thiago.barbosa@uol.com.br");
        ResponseEntity<String> punch = restTemplate.exchange("/api/time-punches", HttpMethod.POST,
                authorized(employee.token(), new PunchRequest(PunchType.ENTRADA)), String.class);
        assertThat(punch.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ResponseEntity<String> sheet = restTemplate.exchange("/api/time-punches/me/timesheet", HttpMethod.GET,
                authorized(employee.token(), null), String.class);
        assertThat(sheet.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sheet.getBody()).contains("ENTRADA");
    }

    @Test
    @DisplayName("exige autenticacao nas rotas protegidas")
    void shouldRequireAuthentication() {
        assertThat(restTemplate.getForEntity("/api/employees", String.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("serve o frontend estatico sem autenticacao")
    void shouldServeStaticFrontend() {
        ResponseEntity<String> page = restTemplate.getForEntity("/index.html", String.class);
        assertThat(page.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(page.getBody()).contains("Piersen");
    }
}

package br.com.academicbit.piersen;

import br.com.academicbit.piersen.domain.Employee;
import br.com.academicbit.piersen.domain.EmployeeStatus;
import br.com.academicbit.piersen.domain.Role;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class TestFixtures {

    public static final LocalDate TODAY = LocalDate.of(2026, 8, 14);

    private TestFixtures() {
    }

    public static Clock fixedClock() {
        return Clock.fixed(ZonedDateTime.of(TODAY.atTime(9, 0), ZoneId.of("America/Sao_Paulo")).toInstant(),
                ZoneId.of("America/Sao_Paulo"));
    }

    public static Employee activeEmployee(Long id) {
        return Employee.builder()
                .id(id)
                .name("Maria Souza")
                .cpf("12345678901")
                .email("maria.souza@piersen.com.br")
                .position("Desenvolvedora")
                .department("Tecnologia")
                .salary(new BigDecimal("6000.00"))
                .admissionDate(TODAY.minusYears(2))
                .status(EmployeeStatus.ATIVO)
                .role(Role.FUNCIONARIO)
                .passwordHash("hash")
                .vacationBalanceDays(30)
                .build();
    }

    public static Employee terminatedEmployee(Long id) {
        Employee employee = activeEmployee(id);
        employee.setStatus(EmployeeStatus.DESLIGADO);
        employee.setTerminationDate(TODAY.minusDays(10));
        return employee;
    }

    public static Employee hrUser(Long id) {
        Employee employee = activeEmployee(id);
        employee.setRole(Role.RH);
        employee.setEmail("rh@piersen.com.br");
        employee.setCpf("00000000000");
        return employee;
    }
}

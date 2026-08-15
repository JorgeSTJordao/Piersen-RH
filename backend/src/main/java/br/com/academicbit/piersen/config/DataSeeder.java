package br.com.academicbit.piersen.config;

import br.com.academicbit.piersen.domain.Employee;
import br.com.academicbit.piersen.domain.EmployeeStatus;
import br.com.academicbit.piersen.domain.MedicalCertificate;
import br.com.academicbit.piersen.domain.Notification;
import br.com.academicbit.piersen.domain.PunchStatus;
import br.com.academicbit.piersen.domain.PunchType;
import br.com.academicbit.piersen.domain.RequestStatus;
import br.com.academicbit.piersen.domain.Role;
import br.com.academicbit.piersen.domain.TimePunch;
import br.com.academicbit.piersen.domain.VacationRequest;
import br.com.academicbit.piersen.repository.EmployeeRepository;
import br.com.academicbit.piersen.repository.MedicalCertificateRepository;
import br.com.academicbit.piersen.repository.NotificationRepository;
import br.com.academicbit.piersen.repository.TimePunchRepository;
import br.com.academicbit.piersen.repository.VacationRequestRepository;
import br.com.academicbit.piersen.service.PayslipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "piersen.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "Piersen@2026";

    private final EmployeeRepository employeeRepository;
    private final TimePunchRepository timePunchRepository;
    private final VacationRequestRepository vacationRequestRepository;
    private final MedicalCertificateRepository certificateRepository;
    private final NotificationRepository notificationRepository;
    private final PayslipService payslipService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (employeeRepository.count() > 0) {
            return;
        }
        Employee carla = save("Carla Menezes", "34876591203", "carla.menezes.rh@gmail.com", "Analista de RH Senior",
                "Recursos Humanos", "9200.00", LocalDate.of(2021, 3, 1), Role.RH, 30);
        Employee pedro = save("Pedro Henrique Alves", "51829463077", "pedro.alves92@hotmail.com", "Desenvolvedor Backend",
                "Tecnologia", "7200.00", LocalDate.of(2022, 6, 13), Role.FUNCIONARIO, 30);
        Employee juliana = save("Juliana Prado", "27604318955", "juliana.prado@outlook.com", "Analista Financeiro",
                "Financeiro", "5400.00", LocalDate.of(2023, 1, 9), Role.FUNCIONARIO, 22);
        Employee rafael = save("Rafael Nogueira", "80435172644", "rafael.nogueira.dev@gmail.com", "Desenvolvedor Frontend",
                "Tecnologia", "6100.00", LocalDate.of(2023, 8, 21), Role.FUNCIONARIO, 30);
        Employee mariana = save("Mariana Castro", "16720459388", "mariana.castro88@yahoo.com.br", "Designer de Produto",
                "Produto", "5800.00", LocalDate.of(2024, 2, 5), Role.FUNCIONARIO, 18);
        Employee thiago = save("Thiago Barbosa", "93217508461", "thiago.barbosa@uol.com.br", "Analista de Suporte",
                "Operacoes", "3900.00", LocalDate.of(2024, 11, 4), Role.FUNCIONARIO, 30);
        Employee larissa = save("Larissa Fontes", "40982371506", "larissa.fontes@bol.com.br", "Assistente Administrativo",
                "Administrativo", "2800.00", LocalDate.of(2022, 4, 18), Role.FUNCIONARIO, 12);

        larissa.setStatus(EmployeeStatus.DESLIGADO);
        larissa.setTerminationDate(LocalDate.now().minusMonths(2));
        employeeRepository.save(larissa);

        seedPunches(pedro);
        seedPunches(rafael);
        seedVacations(juliana, thiago);
        seedCertificate(mariana);
        seedPayslips(pedro, juliana, rafael);
        seedNotifications(pedro, mariana);

        log.info("Base demo do Piersen HR criada. Acesso RH: {} / {}", carla.getEmail(), DEMO_PASSWORD);
    }

    private Employee save(String name, String cpf, String email, String position, String department,
                          String salary, LocalDate admission, Role role, int vacationBalance) {
        return employeeRepository.save(Employee.builder()
                .name(name)
                .cpf(cpf)
                .email(email)
                .position(position)
                .department(department)
                .salary(new BigDecimal(salary))
                .admissionDate(admission)
                .status(EmployeeStatus.ATIVO)
                .role(role)
                .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                .vacationBalanceDays(vacationBalance)
                .build());
    }

    private void seedPunches(Employee employee) {
        punch(employee, LocalDate.now(), LocalTime.of(8, 3), PunchType.ENTRADA, PunchStatus.REGISTRADO);
        punch(employee, LocalDate.now(), LocalTime.of(12, 1), PunchType.INICIO_INTERVALO, PunchStatus.REGISTRADO);
        punch(employee, LocalDate.now(), LocalTime.of(13, 4), PunchType.FIM_INTERVALO, PunchStatus.REGISTRADO);
        punch(employee, LocalDate.now().minusDays(1), LocalTime.of(8, 0), PunchType.ENTRADA, PunchStatus.CONFERIDO);
        punch(employee, LocalDate.now().minusDays(1), LocalTime.of(17, 12), PunchType.SAIDA, PunchStatus.CONFERIDO);
    }

    private void punch(Employee employee, LocalDate day, LocalTime time, PunchType type, PunchStatus status) {
        timePunchRepository.save(TimePunch.builder()
                .employee(employee)
                .punchedAt(LocalDateTime.of(day, time))
                .referenceDay(day)
                .type(type)
                .status(status)
                .build());
    }

    private void seedVacations(Employee juliana, Employee thiago) {
        vacationRequestRepository.save(VacationRequest.builder()
                .employee(juliana)
                .startDate(LocalDate.now().plusDays(45))
                .endDate(LocalDate.now().plusDays(59))
                .daysRequested(15)
                .status(RequestStatus.PENDENTE)
                .requestedAt(LocalDateTime.now().minusDays(2))
                .build());
        vacationRequestRepository.save(VacationRequest.builder()
                .employee(thiago)
                .startDate(LocalDate.now().plusDays(30))
                .endDate(LocalDate.now().plusDays(39))
                .daysRequested(10)
                .status(RequestStatus.PENDENTE)
                .requestedAt(LocalDateTime.now().minusDays(1))
                .build());
        vacationRequestRepository.save(VacationRequest.builder()
                .employee(juliana)
                .startDate(LocalDate.now().minusMonths(6))
                .endDate(LocalDate.now().minusMonths(6).plusDays(7))
                .daysRequested(8)
                .status(RequestStatus.APROVADA)
                .requestedAt(LocalDateTime.now().minusMonths(7))
                .decidedAt(LocalDateTime.now().minusMonths(7).plusDays(1))
                .decisionNote("Aprovado pelo RH")
                .build());
    }

    private void seedCertificate(Employee mariana) {
        certificateRepository.save(MedicalCertificate.builder()
                .employee(mariana)
                .absenceDate(LocalDate.now().minusDays(3))
                .daysOff(2)
                .documentUrl("https://storage.piersen.com.br/atestados/mariana-castro-2026-08.pdf")
                .status(RequestStatus.PENDENTE)
                .submittedAt(LocalDateTime.now().minusDays(2))
                .build());
    }

    private void seedPayslips(Employee... employees) {
        for (Employee employee : employees) {
            payslipService.generate(employee.getId(), YearMonth.now().minusMonths(1));
            payslipService.generate(employee.getId(), YearMonth.now().minusMonths(2));
        }
    }

    private void seedNotifications(Employee pedro, Employee mariana) {
        notify(pedro, "Bem-vindo ao Piersen HR. Suas credenciais de acesso foram enviadas por e-mail.");
        notify(mariana, "Seu atestado foi recebido e esta em analise pelo RH.");
    }

    private void notify(Employee employee, String message) {
        notificationRepository.save(Notification.builder()
                .employee(employee)
                .message(message)
                .createdAt(LocalDateTime.now().minusDays(1))
                .read(false)
                .build());
    }
}

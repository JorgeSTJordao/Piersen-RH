package br.com.academicbit.piersen.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private static final int INITIAL_VACATION_BALANCE_DAYS = 30;

    private final EmployeeRepository employeeRepository;
    private final ContractChangeRepository contractChangeRepository;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;
    private final CredentialMailer credentialMailer;
    private final NotificationService notificationService;
    private final Clock clock;

    @Transactional
    public Employee admit(AdmissionRequest request) {
        if (employeeRepository.existsByCpf(request.cpf())) {
            throw new BusinessException("Ja existe funcionario cadastrado com o CPF informado");
        }
        if (employeeRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("Ja existe funcionario cadastrado com o e-mail informado");
        }
        if (request.admissionDate().isAfter(LocalDate.now(clock))) {
            throw new BusinessException("A data de admissao nao pode ser futura");
        }
        String rawPassword = passwordGenerator.generate();
        Employee saved = employeeRepository.save(Employee.builder()
                .name(request.name())
                .cpf(request.cpf())
                .email(request.email())
                .position(request.position())
                .department(request.department())
                .salary(request.salary())
                .admissionDate(request.admissionDate())
                .status(EmployeeStatus.ATIVO)
                .role(Role.FUNCIONARIO)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .vacationBalanceDays(INITIAL_VACATION_BALANCE_DAYS)
                .build());
        credentialMailer.sendCredentials(saved, rawPassword);
        notificationService.notify(saved, "Bem-vindo ao Piersen HR. Suas credenciais de acesso foram enviadas por e-mail.");
        return saved;
    }

    @Transactional
    public Employee terminate(Long employeeId, LocalDate terminationDate) {
        Employee employee = findById(employeeId);
        if (!employee.isActive()) {
            throw new BusinessException("Funcionario ja esta desligado");
        }
        if (terminationDate.isBefore(employee.getAdmissionDate())) {
            throw new BusinessException("A data de desligamento nao pode ser anterior a admissao");
        }
        employee.setStatus(EmployeeStatus.DESLIGADO);
        employee.setTerminationDate(terminationDate);
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updatePersonalData(Long employeeId, PersonalDataRequest request) {
        Employee employee = findById(employeeId);
        if (!employee.isActive()) {
            throw new BusinessException("Funcionario desligado nao pode alterar dados pessoais");
        }
        if (request.phone() != null) {
            employee.setPhone(request.phone());
        }
        if (request.address() != null) {
            employee.setAddress(request.address());
        }
        if (request.photoUrl() != null) {
            employee.setPhotoUrl(request.photoUrl());
        }
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee changeContract(Long actorId, Long employeeId, ContractChangeRequest request) {
        Employee actor = findById(actorId);
        if (!actor.isHr()) {
            throw new AccessDeniedBusinessException("Apenas o RH pode alterar cargo ou salario");
        }
        Employee employee = findById(employeeId);
        if (!employee.isActive()) {
            throw new BusinessException("Nao e possivel alterar contrato de funcionario desligado");
        }
        contractChangeRepository.save(ContractChange.builder()
                .employee(employee)
                .previousPosition(employee.getPosition())
                .newPosition(request.position())
                .previousSalary(employee.getSalary())
                .newSalary(request.salary())
                .changedAt(LocalDateTime.now(clock))
                .changedBy(actor.getEmail())
                .build());
        employee.setPosition(request.position());
        employee.setSalary(request.salary());
        notificationService.notify(employee, "Seu cargo/salario foi atualizado pelo RH.");
        return employeeRepository.save(employee);
    }

    @Transactional(readOnly = true)
    public Employee findById(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Funcionario nao encontrado: " + employeeId));
    }

    @Transactional(readOnly = true)
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Employee> findByStatus(EmployeeStatus status) {
        return employeeRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<ContractChange> contractHistory(Long employeeId) {
        return contractChangeRepository.findByEmployeeIdOrderByChangedAtDesc(employeeId);
    }

    public Employee requireActive(Long employeeId) {
        Employee employee = findById(employeeId);
        if (!employee.isActive()) {
            throw new AccessDeniedBusinessException("Funcionario desligado nao possui acesso ao portal");
        }
        return employee;
    }
}

package br.com.academicbit.piersen.service;

import br.com.academicbit.piersen.domain.Employee;
import br.com.academicbit.piersen.dto.LoginRequest;
import br.com.academicbit.piersen.dto.LoginResponse;
import br.com.academicbit.piersen.exception.AccessDeniedBusinessException;
import br.com.academicbit.piersen.repository.EmployeeRepository;
import br.com.academicbit.piersen.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Employee employee = employeeRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new AccessDeniedBusinessException("Credenciais invalidas"));
        if (!passwordEncoder.matches(request.password(), employee.getPasswordHash())) {
            throw new AccessDeniedBusinessException("Credenciais invalidas");
        }
        if (!employee.isActive()) {
            throw new AccessDeniedBusinessException("Acesso bloqueado: funcionario desligado");
        }
        return new LoginResponse(jwtService.generate(employee), employee.getId(), employee.getName(), employee.getRole());
    }
}

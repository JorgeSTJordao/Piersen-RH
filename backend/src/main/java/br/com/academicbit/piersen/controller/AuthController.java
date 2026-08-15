package br.com.academicbit.piersen.controller;

import br.com.academicbit.piersen.dto.EmployeeResponse;
import br.com.academicbit.piersen.dto.LoginRequest;
import br.com.academicbit.piersen.dto.LoginResponse;
import br.com.academicbit.piersen.security.CurrentUser;
import br.com.academicbit.piersen.service.AuthService;
import br.com.academicbit.piersen.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmployeeService employeeService;
    private final CurrentUser currentUser;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public EmployeeResponse me() {
        return EmployeeResponse.from(employeeService.findById(currentUser.id()));
    }
}

package br.com.academicbit.piersen.controller;

import br.com.academicbit.piersen.dto.PayslipResponse;
import br.com.academicbit.piersen.security.CurrentUser;
import br.com.academicbit.piersen.service.PayslipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/payslips")
@RequiredArgsConstructor
public class PayslipController {

    private final PayslipService payslipService;
    private final CurrentUser currentUser;

    @PostMapping("/employees/{employeeId}")
    @PreAuthorize("hasRole('RH')")
    @ResponseStatus(HttpStatus.CREATED)
    public PayslipResponse generate(@PathVariable Long employeeId, @RequestParam String reference) {
        return PayslipResponse.from(payslipService.generate(employeeId, YearMonth.parse(reference)));
    }

    @GetMapping("/me")
    public List<PayslipResponse> mine() {
        return payslipService.listFor(currentUser.id()).stream().map(PayslipResponse::from).toList();
    }

    @GetMapping("/employees/{employeeId}")
    @PreAuthorize("hasRole('RH')")
    public List<PayslipResponse> byEmployee(@PathVariable Long employeeId) {
        return payslipService.listFor(employeeId).stream().map(PayslipResponse::from).toList();
    }
}

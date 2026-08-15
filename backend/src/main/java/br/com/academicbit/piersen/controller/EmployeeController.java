package br.com.academicbit.piersen.controller;

import br.com.academicbit.piersen.domain.EmployeeStatus;
import br.com.academicbit.piersen.dto.AdmissionRequest;
import br.com.academicbit.piersen.dto.ContractChangeRequest;
import br.com.academicbit.piersen.dto.EmployeeResponse;
import br.com.academicbit.piersen.dto.PersonalDataRequest;
import br.com.academicbit.piersen.security.CurrentUser;
import br.com.academicbit.piersen.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final CurrentUser currentUser;

    @PostMapping
    @PreAuthorize("hasRole('RH')")
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse admit(@Valid @RequestBody AdmissionRequest request) {
        return EmployeeResponse.from(employeeService.admit(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('RH')")
    public List<EmployeeResponse> list(@RequestParam(required = false) EmployeeStatus status) {
        return (status == null ? employeeService.findAll() : employeeService.findByStatus(status))
                .stream().map(EmployeeResponse::from).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('RH')")
    public EmployeeResponse findById(@PathVariable Long id) {
        return EmployeeResponse.from(employeeService.findById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RH')")
    public EmployeeResponse terminate(@PathVariable Long id,
                                      @RequestParam(required = false) LocalDate terminationDate) {
        return EmployeeResponse.from(employeeService.terminate(id,
                terminationDate == null ? LocalDate.now() : terminationDate));
    }

    @PutMapping("/{id}/contract")
    @PreAuthorize("hasRole('RH')")
    public EmployeeResponse changeContract(@PathVariable Long id, @Valid @RequestBody ContractChangeRequest request) {
        return EmployeeResponse.from(employeeService.changeContract(currentUser.id(), id, request));
    }

    @PutMapping("/me/personal-data")
    public EmployeeResponse updatePersonalData(@Valid @RequestBody PersonalDataRequest request) {
        return EmployeeResponse.from(employeeService.updatePersonalData(currentUser.id(), request));
    }
}

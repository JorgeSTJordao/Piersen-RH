package br.com.academicbit.piersen.dto;

import br.com.academicbit.piersen.domain.Employee;
import br.com.academicbit.piersen.domain.EmployeeStatus;
import br.com.academicbit.piersen.domain.Role;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        String name,
        String cpf,
        String email,
        String position,
        String department,
        BigDecimal salary,
        LocalDate admissionDate,
        LocalDate terminationDate,
        EmployeeStatus status,
        Role role,
        String phone,
        String address,
        String photoUrl,
        Integer vacationBalanceDays) {

    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(employee.getId(), employee.getName(), employee.getCpf(), employee.getEmail(),
                employee.getPosition(), employee.getDepartment(), employee.getSalary(), employee.getAdmissionDate(),
                employee.getTerminationDate(), employee.getStatus(), employee.getRole(), employee.getPhone(),
                employee.getAddress(), employee.getPhotoUrl(), employee.getVacationBalanceDays());
    }
}

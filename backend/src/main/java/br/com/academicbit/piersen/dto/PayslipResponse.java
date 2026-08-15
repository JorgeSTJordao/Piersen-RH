package br.com.academicbit.piersen.dto;

import br.com.academicbit.piersen.domain.Payslip;

import java.math.BigDecimal;

public record PayslipResponse(
        Long id,
        Long employeeId,
        String referenceMonth,
        BigDecimal grossSalary,
        BigDecimal deductions,
        BigDecimal netSalary) {

    public static PayslipResponse from(Payslip payslip) {
        return new PayslipResponse(payslip.getId(), payslip.getEmployee().getId(), payslip.getReferenceMonth(),
                payslip.getGrossSalary(), payslip.getDeductions(), payslip.getNetSalary());
    }
}

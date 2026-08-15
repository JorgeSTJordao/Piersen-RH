package br.com.academicbit.piersen.service;

import br.com.academicbit.piersen.domain.Employee;
import br.com.academicbit.piersen.domain.Payslip;
import br.com.academicbit.piersen.exception.BusinessException;
import br.com.academicbit.piersen.repository.PayslipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayslipService {

    private static final BigDecimal INSS_RATE = new BigDecimal("0.11");
    private static final BigDecimal IRRF_RATE = new BigDecimal("0.075");
    private static final BigDecimal IRRF_THRESHOLD = new BigDecimal("2259.20");

    private final PayslipRepository payslipRepository;
    private final EmployeeService employeeService;

    @Transactional
    public Payslip generate(Long employeeId, YearMonth reference) {
        Employee employee = employeeService.findById(employeeId);
        String month = reference.toString();
        if (payslipRepository.findByEmployeeIdAndReferenceMonth(employeeId, month).isPresent()) {
            throw new BusinessException("Holerite ja gerado para a competencia " + month);
        }
        if (reference.isBefore(YearMonth.from(employee.getAdmissionDate()))) {
            throw new BusinessException("Competencia anterior a admissao do funcionario");
        }
        BigDecimal gross = employee.getSalary();
        BigDecimal deductions = deductionsFor(gross);
        return payslipRepository.save(Payslip.builder()
                .employee(employee)
                .referenceMonth(month)
                .grossSalary(gross)
                .deductions(deductions)
                .netSalary(gross.subtract(deductions).setScale(2, RoundingMode.HALF_UP))
                .build());
    }

    BigDecimal deductionsFor(BigDecimal gross) {
        BigDecimal inss = gross.multiply(INSS_RATE);
        BigDecimal base = gross.subtract(inss);
        BigDecimal irrf = base.compareTo(IRRF_THRESHOLD) > 0 ? base.multiply(IRRF_RATE) : BigDecimal.ZERO;
        return inss.add(irrf).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public List<Payslip> listFor(Long employeeId) {
        return payslipRepository.findByEmployeeIdOrderByReferenceMonthDesc(employeeId);
    }
}

package br.com.academicbit.piersen.repository;

import br.com.academicbit.piersen.domain.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayslipRepository extends JpaRepository<Payslip, Long> {

    List<Payslip> findByEmployeeIdOrderByReferenceMonthDesc(Long employeeId);

    Optional<Payslip> findByEmployeeIdAndReferenceMonth(Long employeeId, String referenceMonth);
}

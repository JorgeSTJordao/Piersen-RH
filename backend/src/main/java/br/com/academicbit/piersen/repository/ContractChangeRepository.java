package br.com.academicbit.piersen.repository;

import br.com.academicbit.piersen.domain.ContractChange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractChangeRepository extends JpaRepository<ContractChange, Long> {

    List<ContractChange> findByEmployeeIdOrderByChangedAtDesc(Long employeeId);
}

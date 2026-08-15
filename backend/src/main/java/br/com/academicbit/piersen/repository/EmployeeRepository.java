package br.com.academicbit.piersen.repository;

import br.com.academicbit.piersen.domain.Employee;
import br.com.academicbit.piersen.domain.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmailIgnoreCase(String email);

    boolean existsByCpf(String cpf);

    boolean existsByEmailIgnoreCase(String email);

    List<Employee> findByStatus(EmployeeStatus status);
}

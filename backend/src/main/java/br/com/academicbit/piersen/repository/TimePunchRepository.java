package br.com.academicbit.piersen.repository;

import br.com.academicbit.piersen.domain.PunchStatus;
import br.com.academicbit.piersen.domain.TimePunch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TimePunchRepository extends JpaRepository<TimePunch, Long> {

    List<TimePunch> findByEmployeeIdAndReferenceDayOrderByPunchedAtAsc(Long employeeId, LocalDate referenceDay);

    List<TimePunch> findByEmployeeIdOrderByPunchedAtDesc(Long employeeId);

    List<TimePunch> findByStatusOrderByPunchedAtAsc(PunchStatus status);
}

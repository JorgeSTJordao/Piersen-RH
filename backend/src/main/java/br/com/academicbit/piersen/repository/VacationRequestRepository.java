package br.com.academicbit.piersen.repository;

import br.com.academicbit.piersen.domain.RequestStatus;
import br.com.academicbit.piersen.domain.VacationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface VacationRequestRepository extends JpaRepository<VacationRequest, Long> {

    List<VacationRequest> findByEmployeeIdOrderByRequestedAtDesc(Long employeeId);

    List<VacationRequest> findByStatusOrderByRequestedAtAsc(RequestStatus status);

    List<VacationRequest> findByEmployeeIdAndStatusInAndEndDateGreaterThanEqualAndStartDateLessThanEqual(
            Long employeeId, List<RequestStatus> statuses, LocalDate start, LocalDate end);
}

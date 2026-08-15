package br.com.academicbit.piersen.repository;

import br.com.academicbit.piersen.domain.MedicalCertificate;
import br.com.academicbit.piersen.domain.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalCertificateRepository extends JpaRepository<MedicalCertificate, Long> {

    List<MedicalCertificate> findByEmployeeIdOrderBySubmittedAtDesc(Long employeeId);

    List<MedicalCertificate> findByStatusOrderBySubmittedAtAsc(RequestStatus status);
}

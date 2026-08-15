package br.com.academicbit.piersen.repository;

import br.com.academicbit.piersen.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
}

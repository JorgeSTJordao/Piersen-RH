package br.com.academicbit.piersen.service;

import br.com.academicbit.piersen.domain.Employee;
import br.com.academicbit.piersen.domain.Notification;
import br.com.academicbit.piersen.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final Clock clock;

    @Transactional
    public Notification notify(Employee employee, String message) {
        return notificationRepository.save(Notification.builder()
                .employee(employee)
                .message(message)
                .createdAt(LocalDateTime.now(clock))
                .read(false)
                .build());
    }

    @Transactional(readOnly = true)
    public List<Notification> listFor(Long employeeId) {
        return notificationRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }

    @Transactional
    public void markAllAsRead(Long employeeId) {
        List<Notification> notifications = notificationRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
        for (Notification notification : notifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(notifications);
    }
}

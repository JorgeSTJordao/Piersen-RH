package br.com.academicbit.piersen.controller;

import br.com.academicbit.piersen.dto.NotificationResponse;
import br.com.academicbit.piersen.security.CurrentUser;
import br.com.academicbit.piersen.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUser currentUser;

    @GetMapping("/me")
    public List<NotificationResponse> mine() {
        return notificationService.listFor(currentUser.id()).stream().map(NotificationResponse::from).toList();
    }

    @PutMapping("/me/read")
    public void markAllAsRead() {
        notificationService.markAllAsRead(currentUser.id());
    }
}

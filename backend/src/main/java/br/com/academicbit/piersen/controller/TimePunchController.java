package br.com.academicbit.piersen.controller;

import br.com.academicbit.piersen.domain.PunchStatus;
import br.com.academicbit.piersen.dto.PunchRequest;
import br.com.academicbit.piersen.dto.PunchResponse;
import br.com.academicbit.piersen.dto.TimeSheetResponse;
import br.com.academicbit.piersen.security.CurrentUser;
import br.com.academicbit.piersen.service.TimePunchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/time-punches")
@RequiredArgsConstructor
public class TimePunchController {

    private final TimePunchService timePunchService;
    private final CurrentUser currentUser;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PunchResponse punch(@Valid @RequestBody PunchRequest request) {
        return PunchResponse.from(timePunchService.punch(currentUser.id(), request.type()));
    }

    @GetMapping("/me")
    public List<PunchResponse> myHistory() {
        return timePunchService.history(currentUser.id()).stream().map(PunchResponse::from).toList();
    }

    @GetMapping("/me/timesheet")
    public TimeSheetResponse myTimeSheet(@RequestParam(required = false)
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day) {
        return timePunchService.timeSheet(currentUser.id(), day == null ? LocalDate.now() : day);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('RH')")
    public List<PunchResponse> pending() {
        return timePunchService.pendingReview().stream().map(PunchResponse::from).toList();
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('RH')")
    public PunchResponse review(@PathVariable Long id, @RequestParam PunchStatus status) {
        return PunchResponse.from(timePunchService.review(id, status));
    }
}

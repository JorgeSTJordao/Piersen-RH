package br.com.academicbit.piersen.controller;

import br.com.academicbit.piersen.dto.DecisionRequest;
import br.com.academicbit.piersen.dto.VacationRequestInput;
import br.com.academicbit.piersen.dto.VacationResponse;
import br.com.academicbit.piersen.security.CurrentUser;
import br.com.academicbit.piersen.service.VacationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vacations")
@RequiredArgsConstructor
public class VacationController {

    private final VacationService vacationService;
    private final CurrentUser currentUser;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VacationResponse request(@Valid @RequestBody VacationRequestInput input) {
        return VacationResponse.from(vacationService.request(currentUser.id(), input));
    }

    @GetMapping("/me")
    public List<VacationResponse> mine() {
        return vacationService.listFor(currentUser.id()).stream().map(VacationResponse::from).toList();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('RH')")
    public List<VacationResponse> pending() {
        return vacationService.listPending().stream().map(VacationResponse::from).toList();
    }

    @PutMapping("/{id}/decision")
    @PreAuthorize("hasRole('RH')")
    public VacationResponse decide(@PathVariable Long id, @Valid @RequestBody DecisionRequest decision) {
        return VacationResponse.from(vacationService.decide(id, decision.approved(), decision.note()));
    }
}

package br.com.academicbit.piersen.controller;

import br.com.academicbit.piersen.dto.CertificateRequestInput;
import br.com.academicbit.piersen.dto.CertificateResponse;
import br.com.academicbit.piersen.dto.DecisionRequest;
import br.com.academicbit.piersen.security.CurrentUser;
import br.com.academicbit.piersen.service.CertificateService;
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
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;
    private final CurrentUser currentUser;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CertificateResponse submit(@Valid @RequestBody CertificateRequestInput input) {
        return CertificateResponse.from(certificateService.submit(currentUser.id(), input));
    }

    @GetMapping("/me")
    public List<CertificateResponse> mine() {
        return certificateService.listFor(currentUser.id()).stream().map(CertificateResponse::from).toList();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('RH')")
    public List<CertificateResponse> pending() {
        return certificateService.listPending().stream().map(CertificateResponse::from).toList();
    }

    @PutMapping("/{id}/decision")
    @PreAuthorize("hasRole('RH')")
    public CertificateResponse decide(@PathVariable Long id, @Valid @RequestBody DecisionRequest decision) {
        return CertificateResponse.from(certificateService.decide(id, decision.approved(), decision.note()));
    }
}

package br.com.academicbit.piersen.security;

import br.com.academicbit.piersen.exception.AccessDeniedBusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public Long id() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long employeeId)) {
            throw new AccessDeniedBusinessException("Usuario nao autenticado");
        }
        return employeeId;
    }
}

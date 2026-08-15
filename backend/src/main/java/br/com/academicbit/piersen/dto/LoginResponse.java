package br.com.academicbit.piersen.dto;

import br.com.academicbit.piersen.domain.Role;

public record LoginResponse(String token, Long employeeId, String name, Role role) {
}

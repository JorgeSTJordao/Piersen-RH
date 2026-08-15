package br.com.academicbit.piersen.dto;

import jakarta.validation.constraints.Size;

public record PersonalDataRequest(
        @Size(max = 20) String phone,
        @Size(max = 200) String address,
        @Size(max = 300) String photoUrl) {
}

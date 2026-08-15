package br.com.academicbit.piersen.service;

import br.com.academicbit.piersen.domain.Employee;

public interface CredentialMailer {

    void sendCredentials(Employee employee, String rawPassword);
}

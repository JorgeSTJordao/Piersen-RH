package br.com.academicbit.piersen.service;

import br.com.academicbit.piersen.domain.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LoggingCredentialMailer implements CredentialMailer {

    @Override
    public void sendCredentials(Employee employee, String rawPassword) {
        log.info("Credenciais do Portal Piersen HR enviadas para {} | usuario={} senha={}",
                employee.getEmail(), employee.getEmail(), rawPassword);
    }
}

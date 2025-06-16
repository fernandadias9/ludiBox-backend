package br.com.ludibox.scheduler;

import br.com.ludibox.service.GoogleAuthenticatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class GenerateSecretScheduler {

    @Autowired
    private final GoogleAuthenticatorService googleAuthenticatorService;

    public GenerateSecretScheduler(GoogleAuthenticatorService googleAuthenticatorService) {
        this.googleAuthenticatorService = googleAuthenticatorService;
    }

    @Scheduled(fixedRate = 1000L)
    public void getCode() {
        Date timestamp = new Date(System.currentTimeMillis());
        String code = googleAuthenticatorService.getCode(timestamp);
        System.out.println("Código TOTP gerado: " + code);
    }
}

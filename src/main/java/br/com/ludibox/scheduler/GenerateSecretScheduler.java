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

}

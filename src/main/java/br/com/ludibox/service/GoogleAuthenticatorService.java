package br.com.ludibox.service;

import br.com.ludibox.components.authenticator.google.CodeGenerator;
import com.bastiaanjansen.otp.HMACAlgorithm;
import com.bastiaanjansen.otp.TOTPGenerator;
import com.bastiaanjansen.otp.SecretGenerator;
import com.google.zxing.WriterException;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class GoogleAuthenticatorService {

    private final CodeGenerator generator;

    public GoogleAuthenticatorService(CodeGenerator generator) {
        this.generator = generator;
    }



    public BufferedImage generateQRCode(String issuer, String email, String secretBase32)
            throws WriterException, URISyntaxException {
        byte[] secret = new Base32().decode(secretBase32);
        return generator.generate(issuer, email, secret);
    }

    public String generateSecretBase32() {
        byte[] secret = com.bastiaanjansen.otp.SecretGenerator.generate();
        return new Base32().encodeToString(secret).replace("=", "");
    }

    public boolean isCodeValid(String secretBase32, String code) {
        byte[] secret = new Base32().decode(secretBase32);

        TOTPGenerator totp = new TOTPGenerator.Builder(secret)
                .withHOTPGenerator(builder -> {
                    builder.withPasswordLength(6);
                    builder.withAlgorithm(HMACAlgorithm.SHA1);
                })
                .withPeriod(Duration.ofSeconds(30))
                .build();

        Instant now = Instant.now();

        return code.equals(totp.at(now.minusSeconds(30))) ||
                code.equals(totp.at(now)) ||
                code.equals(totp.at(now.plusSeconds(30)));
    }

}

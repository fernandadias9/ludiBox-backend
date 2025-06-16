package br.com.ludibox.service;

import br.com.ludibox.components.authenticator.google.CodeGenerator;
import com.bastiaanjansen.otp.TOTPGenerator;
import com.google.zxing.WriterException;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.net.URISyntaxException;
import java.util.Date;

@Service
public class GoogleAuthenticatorService {

    private final CodeGenerator generator;
    private final byte[] secret;
    private final TOTPGenerator totp;

    public GoogleAuthenticatorService(CodeGenerator generator) {
        this.generator = generator;

        this.secret = com.bastiaanjansen.otp.SecretGenerator.generate();

        this.totp = new TOTPGenerator.Builder(secret)
                .withHOTPGenerator(builder -> {
                    builder.withPasswordLength(6);
                    builder.withAlgorithm(com.bastiaanjansen.otp.HMACAlgorithm.SHA1);
                })
                .withPeriod(java.time.Duration.ofSeconds(30))
                .build();
    }

    public String getCode(Date timestamp) {
        return totp.at(timestamp.toInstant());
    }

    public BufferedImage generateCode(String issuer, String email)
            throws WriterException, URISyntaxException {
        return generator.generate(issuer, email, secret);
    }


    public String getSecretBase32() {
        Base32 base32 = new Base32();
        return base32.encodeToString(secret).replace("=", "");
    }
}

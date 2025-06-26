package br.com.ludibox.service;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.components.authenticator.google.CodeGenerator;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.repository.PessoaRepository;
import com.bastiaanjansen.otp.HMACAlgorithm;
import com.bastiaanjansen.otp.TOTPGenerator;
import com.google.zxing.WriterException;
import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;

@Service
public class GoogleAuthenticatorService {

    private final CodeGenerator generator;

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private PessoaRepository pessoaRepository;

    public GoogleAuthenticatorService(CodeGenerator generator) {
        this.generator = generator;
    }



    public BufferedImage generateQRCode(String issuer, String email, String secretBase32)
            throws WriterException, URISyntaxException {
        byte[] secret = new Base32().decode(secretBase32);
        return generator.generate(issuer, email, secret);
    }



    public BufferedImage gerarQRCodeParaUsuario() {
        Pessoa pessoaAutenticada = authService.getPessoaAutenticada();
        Pessoa pessoaAtualizada = pessoaRepository.findById(pessoaAutenticada.getId())
                .orElseThrow(() -> new LudiBoxException("Erro", "Usuário não encontrado.", HttpStatus.NOT_FOUND));

        if (pessoaAtualizada.getSecretTotp() == null || pessoaAtualizada.getSecretTotp().isBlank()) {
            throw new LudiBoxException("Erro", "2FA não ativado para este usuário.", HttpStatus.BAD_REQUEST);
        }

        try {
            return generateQRCode("LudiBox", pessoaAtualizada.getEmail(), pessoaAtualizada.getSecretTotp());
        } catch (Exception e) {
            throw new LudiBoxException("Erro", "Falha ao gerar QR Code", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public String generateSecretBase32() {
        byte[] secret = com.bastiaanjansen.otp.SecretGenerator.generate();
        return new Base32().encodeToString(secret).replace("=", "");
    }

    public boolean isCodeValid(String secretBase32, String code) {
        code = code.trim();

        byte[] secret = new Base32().decode(secretBase32);

        TOTPGenerator totp = new TOTPGenerator.Builder(secret)
                .withHOTPGenerator(builder -> {
                    builder.withPasswordLength(6);
                    builder.withAlgorithm(HMACAlgorithm.SHA1);
                })
                .withPeriod(Duration.ofSeconds(30))
                .build();

        Instant now = Instant.now();

        String codeMinus30 = totp.at(now.minusSeconds(30));
        String codeNow = totp.at(now);
        String codePlus30 = totp.at(now.plusSeconds(30));

        boolean valid = code.equals(codeMinus30) || code.equals(codeNow) || code.equals(codePlus30);

        return valid;
    }


}

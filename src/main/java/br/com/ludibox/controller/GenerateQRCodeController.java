package br.com.ludibox.controller;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.repository.PessoaRepository;
import br.com.ludibox.service.GoogleAuthenticatorService;
import br.com.ludibox.service.PessoaService;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

@RestController
@RequestMapping(path = "/two-factors")
public class GenerateQRCodeController {

    @Autowired
    private GoogleAuthenticatorService googleAuthenticatorService;

    @Autowired
    private PessoaService pessoaService;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private AuthenticationService authService;

    public GenerateQRCodeController(GoogleAuthenticatorService googleAuthenticatorService) {
        this.googleAuthenticatorService = googleAuthenticatorService;
    }

    @GetMapping(value = "/generate-qr", produces = MediaType.IMAGE_PNG_VALUE)
    public BufferedImage generateQRCodeParaUsuario() {
        return googleAuthenticatorService.gerarQRCodeParaUsuario();
    }

    @PostMapping("/2fa/generate")
    public ResponseEntity<byte[]> generateQrCode()  throws URISyntaxException{
        Pessoa pessoaAutenticada = authService.getPessoaAutenticada();
        try {
            Pessoa pessoa = pessoaRepository.findById(pessoaAutenticada.getId()).get();

            String secret = googleAuthenticatorService.generateSecretBase32();
            pessoa.setSecretTotp(secret);
            pessoa.setTwoFactorEnabled(true);
            pessoa.setTwoFactorConfirmed(false);

            pessoaRepository.save(pessoa);

            String issuer = "LudiBox";
            BufferedImage qrCode = googleAuthenticatorService.generateQRCode(issuer, pessoa.getEmail(), secret);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrCode, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);

        } catch (WriterException | IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }



    @PostMapping("/2fa/confirm")
    public ResponseEntity<?> confirm2FA(Authentication auth, @RequestParam("code") String code) {
        Pessoa pessoaAutenticada = authService.getPessoaAutenticada();
        Pessoa pessoa = pessoaRepository.findById(pessoaAutenticada.getId()).get();

        if (!googleAuthenticatorService.isCodeValid(pessoa.getSecretTotp(), code)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Código inválido");
        }

        pessoa.setTwoFactorConfirmed(true);
        pessoaRepository.save(pessoa);

        return ResponseEntity.ok("2FA confirmado com sucesso");
    }

    @PostMapping("/2fa/toggle")
    public ResponseEntity<?> toggle2FA(@RequestBody Boolean enable) {
        Pessoa pessoaAutenticada = authService.getPessoaAutenticada();
        Pessoa pessoa = pessoaRepository.findById(pessoaAutenticada.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (Boolean.TRUE.equals(enable)) {
            String secret = googleAuthenticatorService.generateSecretBase32();
            pessoa.setTwoFactorEnabled(true);
            pessoa.setTwoFactorConfirmed(false);
        } else {
            pessoa.setTwoFactorEnabled(false);
            pessoa.setTwoFactorConfirmed(false);
        }

        pessoaRepository.save(pessoa);

        return ResponseEntity.ok("2FA " + (Boolean.TRUE.equals(enable) ? "ativado" : "desativado") + " com sucesso");
    }






}

package br.com.ludibox.controller;

import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.service.GoogleAuthenticatorService;
import br.com.ludibox.service.PessoaService;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.net.URISyntaxException;

@RestController
@RequestMapping(path = "/public")
public class GenerateQRCodeController {

    @Autowired
    private GoogleAuthenticatorService googleAuthenticatorService;

    @Autowired
    private PessoaService pessoaService;

    public GenerateQRCodeController(GoogleAuthenticatorService googleAuthenticatorService) {
        this.googleAuthenticatorService = googleAuthenticatorService;
    }

    @GetMapping(value = "/generate-qr/{email}", produces = MediaType.IMAGE_PNG_VALUE)
    public BufferedImage generateQRCode(@PathVariable String email) throws Exception {
        Pessoa pessoa = pessoaService.buscarPorEmail(email);

        if (pessoa == null || pessoa.getSecretTotp() == null || pessoa.getSecretTotp().isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado ou secret TOTP não definido.");
        }

        return googleAuthenticatorService.generateQRCode("LudiBox", pessoa.getEmail(), pessoa.getSecretTotp());
    }

}

package br.com.ludibox.controller;

import br.com.ludibox.service.GoogleAuthenticatorService;
import com.google.zxing.WriterException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/qrcode")
public class GenerateQRCodeController {

    private final GoogleAuthenticatorService googleAuthenticatorService;

    public GenerateQRCodeController(GoogleAuthenticatorService googleAuthenticatorService) {
        this.googleAuthenticatorService = googleAuthenticatorService;
    }

    @GetMapping(value = "/generate/{issuer}/{email}", produces = MediaType.IMAGE_PNG_VALUE)
    public BufferedImage generate(
            @PathVariable String issuer,
            @PathVariable String email
    ) throws WriterException, URISyntaxException {
        return googleAuthenticatorService.generateCode(issuer, email);
    }
}

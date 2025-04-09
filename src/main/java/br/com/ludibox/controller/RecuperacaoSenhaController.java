package br.com.ludibox.controller;

import br.com.ludibox.service.ResetarSenhaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/password")
public class RecuperacaoSenhaController {

    @Autowired
    private ResetarSenhaService resetarSenhaService;

    @PostMapping("/reset")
    public ResponseEntity<String> resetarSenha(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        resetarSenhaService.resetarSenha(email);
        return ResponseEntity.ok("Sua nova senha foi enviada para seu e-mail.");
    }
}

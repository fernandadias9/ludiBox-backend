package br.com.ludibox.controller;

import br.com.ludibox.service.ModeradorIA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/public/ia")
public class ModeradorIaController {

    @Autowired
    private ModeradorIA moderadorIA;


    @PostMapping
    public String testarIA(@RequestBody Map<String, String> body) {
        String mensagem = body.get("mensagem");
        return moderadorIA.processarTexto(mensagem);
    }
}

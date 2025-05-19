package br.com.ludibox.controller;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Produto;
import br.com.ludibox.service.ModeradorIA;
import br.com.ludibox.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/public/ia")
public class ModeradorIaController {

    private final ModeradorIA moderadorIA;

    @Autowired
    public ModeradorIaController(ModeradorIA moderadorIA) {
        this.moderadorIA = moderadorIA;
    }

    @PostMapping(path = "/testar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> testarIA(@RequestBody Produto produtoOriginal) {
        try {
            Produto produtoAtualizado = moderadorIA.validarConteudoProduto(produtoOriginal);
            return ResponseEntity.ok(produtoAtualizado);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("erro", "Violação das diretrizes", "detalhes", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao processar com IA", "detalhes", e.getMessage()));
        }
    }
}
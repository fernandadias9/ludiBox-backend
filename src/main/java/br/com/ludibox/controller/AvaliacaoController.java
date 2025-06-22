package br.com.ludibox.controller;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.model.dto.AvaliacaoRequestDTO;
import br.com.ludibox.model.entity.Avaliacao;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.service.AuthService;
import br.com.ludibox.service.AvaliacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoController {
    @Autowired
    AvaliacaoService avaliacaoService;
    @Autowired
    private AuthenticationService authService;

    @PostMapping
    public ResponseEntity<Avaliacao> salvar(
            @Valid @RequestBody AvaliacaoRequestDTO req
    ) {
        Pessoa pessoaLogada = authService.getPessoaAutenticada();

        Avaliacao a = avaliacaoService.salvar(
                req.getProdutoLocacaoId(),
                pessoaLogada.getId(),
                req.getEstrelas(),
                req.getComentario()
        );
        return ResponseEntity.ok(a);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Avaliacao> alterar(
            @PathVariable Long id,
            @RequestParam int estrelas,
            @AuthenticationPrincipal(expression = "id") Long avaliadorId) {
        Avaliacao a = avaliacaoService.alterar(id, estrelas, avaliadorId);
        return ResponseEntity.ok(a);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "id") Long avaliadorId) {
        avaliacaoService.deletar(id, avaliadorId);
        return ResponseEntity.noContent().build();
    }
}

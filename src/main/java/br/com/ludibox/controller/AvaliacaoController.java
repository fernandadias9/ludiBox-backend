package br.com.ludibox.controller;

import br.com.ludibox.model.entity.Avaliacao;
import br.com.ludibox.service.AvaliacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoController {
    @Autowired
    AvaliacaoService avaliacaoService;

    @PostMapping
    public ResponseEntity<Avaliacao> salvar(
            @RequestParam Integer produtoLocacaoId,
            @RequestParam int estrelas,
            @AuthenticationPrincipal(expression = "id") Integer avaliadorId) {
        Avaliacao a = avaliacaoService.salvar(produtoLocacaoId, avaliadorId, estrelas);
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

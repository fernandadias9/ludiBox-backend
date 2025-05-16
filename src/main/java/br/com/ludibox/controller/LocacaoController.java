package br.com.ludibox.controller;

import br.com.ludibox.model.entity.Locacao;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.entity.ProdutoLocacao;
import br.com.ludibox.service.LocacaoService;
import br.com.ludibox.service.PessoaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/locacao")
public class LocacaoController {
    @Autowired
    private LocacaoService locacaoService;

    @Autowired
    private PessoaService pessoaService;

    @PostMapping
    public ResponseEntity<Locacao> abrirNovaLocacao(@RequestBody @Valid Locacao locacao) {
        Locacao novaLocacao = locacaoService.abrirNovaLocacao(locacao);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaLocacao);
    }

    @PostMapping("/{id}/produtos")
    public ResponseEntity<Locacao> incluirProdutoNaLocacao(
            @PathVariable Integer id,
            @RequestBody ProdutoLocacao produtoLocacao
    ) {
        Locacao locacaoAtualizada = locacaoService.incluirProdutoNaLocacao(id, produtoLocacao);
        return ResponseEntity.ok(locacaoAtualizada);
    }

    @DeleteMapping("/{id}/produtos/{produtoLocacaoId}")
    public ResponseEntity<Locacao> retirarProdutoDaLocacao(
            @PathVariable Integer id,
            @PathVariable Integer produtoLocacaoId
    ) {
        Locacao locacaoAtualizada = locacaoService.retirarProdutoDaLocacao(id, produtoLocacaoId);
        return ResponseEntity.ok(locacaoAtualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarLocacao(@PathVariable Integer id) {
        locacaoService.deletarLocacao(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<String> cancelarLocacao(
            @PathVariable Integer id,
            @RequestParam String motivoCancelamento) {
        try {
            locacaoService.cancelarLocacao(id, motivoCancelamento);
            return ResponseEntity.ok("Locação cancelada com sucesso.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{locacaoId}/endereco-entrega/{enderecoId}")
    public ResponseEntity<Void> escolherEnderecoEntrega(
            @PathVariable Integer locacaoId,
            @PathVariable Integer enderecoId,
            @RequestParam Integer locadorId) {
        locacaoService.escolherEnderecoEntrega(locacaoId, enderecoId, locadorId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pendente/{usuarioId}")
    public ResponseEntity<Locacao> buscarLocacaoPendente(@PathVariable Integer usuarioId) {
        return locacaoService
                .buscarLocacaoPendentePorUsuarioId(usuarioId)
                .map(loc -> ResponseEntity.ok(loc))
                .orElseGet(() -> ResponseEntity.ok().body(null));
    }
}

package br.com.ludibox.controller;

import br.com.ludibox.model.enums.EnumStatusDenuncia;
import br.com.ludibox.model.enums.EnumMotivoDenuncia;
import br.com.ludibox.model.entity.Denuncia;
import br.com.ludibox.model.enums.StatusProduto;
import br.com.ludibox.model.repository.DenunciaRepository;
import br.com.ludibox.model.repository.ProdutoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/denuncias")
@RequiredArgsConstructor
public class DenunciaController {

    private final DenunciaRepository denunciaRepository;
    private final ProdutoRepository produtoRepository;

    @PostMapping
    public ResponseEntity<Denuncia> criar(@Valid @RequestBody Denuncia denuncia) {
        return ResponseEntity.ok(denunciaRepository.save(denuncia));
    }

    @GetMapping("/filtro")
    public ResponseEntity<List<Denuncia>> filtrar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) EnumMotivoDenuncia motivo,
            @RequestParam(required = false) EnumStatusDenuncia status
    ) {
        return ResponseEntity.ok(
                denunciaRepository.buscarComFiltros(dataInicio, dataFim, motivo, status)
        );
    }

    @PutMapping("/{id}/permitir")
    public ResponseEntity<?> permitir(@PathVariable Integer id) {
        return denunciaRepository.findById(id).map(denuncia -> {
            denuncia.setStatus(EnumStatusDenuncia.ANALISADO);
            denunciaRepository.save(denuncia);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/bloquear")
    public ResponseEntity<?> bloquear(@PathVariable Integer id) {
        return denunciaRepository.findById(id).map(denuncia -> {
            denuncia.getProduto().setStatus(StatusProduto.INATIVO);
            produtoRepository.save(denuncia.getProduto());

            denuncia.setStatus(EnumStatusDenuncia.ANALISADO);
            denunciaRepository.save(denuncia);

            return ResponseEntity.noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
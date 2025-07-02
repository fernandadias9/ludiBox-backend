package br.com.ludibox.controller;

import br.com.ludibox.model.dto.FiltroPagamentoDTO;
import br.com.ludibox.model.entity.PagamentosAnunciante;
import br.com.ludibox.service.PagamentosAnuncianteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/pagamentos")
public class PagamentosAnuncianteController {

    @Autowired
    private PagamentosAnuncianteService service;

    @PostMapping
    public ResponseEntity<PagamentosAnunciante> salvar(@RequestBody PagamentosAnunciante novoPagamento) {
        PagamentosAnunciante saved = service.salvar(novoPagamento);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/filtrar")
    public ResponseEntity<List<PagamentosAnunciante>> listarComFiltro(@RequestBody FiltroPagamentoDTO filtro) {
        List<PagamentosAnunciante> lista = service.listarComFiltro(filtro);
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/pagar/{id}")
    public ResponseEntity<?> marcarComoPago(@PathVariable Integer id) {
        service.marcarComoPago(id);
        return ResponseEntity.ok().build();
    }
}


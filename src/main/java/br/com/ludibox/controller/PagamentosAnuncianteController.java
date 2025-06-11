package br.com.ludibox.controller;

import br.com.ludibox.model.entity.PagamentosAnunciante;
import br.com.ludibox.service.PagamentosAnuncianteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

package br.com.ludibox.controller;

import br.com.ludibox.model.entity.PessoaJuridica;
import br.com.ludibox.service.PessoaJuridicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pessoa-juridica")
public class PessoaJuridicaController {

    @Autowired
    private PessoaJuridicaService service;

    @PostMapping
    public ResponseEntity<PessoaJuridica> salvar(@RequestBody PessoaJuridica pessoaJuridica) {
        return ResponseEntity.ok(service.salvar(pessoaJuridica));
    }
}

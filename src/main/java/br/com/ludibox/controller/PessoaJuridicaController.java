package br.com.ludibox.controller;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.PessoaFisica;
import br.com.ludibox.model.entity.PessoaJuridica;
import br.com.ludibox.service.PessoaJuridicaService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pessoa-juridica")
public class PessoaJuridicaController {

    @Autowired
    private PessoaJuridicaService service;

    @PostMapping
    public ResponseEntity<PessoaJuridica> salvar(@RequestBody PessoaJuridica pessoaJuridica) throws LudiBoxException {
        return ResponseEntity.ok(service.salvar(pessoaJuridica));
    }
    
    @PutMapping
    public ResponseEntity<PessoaJuridica> atualizarPj(@RequestBody PessoaJuridica pessoaJuridica) throws LudiBoxException {
        return ResponseEntity.ok(service.atualizarDados(pessoaJuridica));
    }
    
    @PutMapping("/desativar")
    public void desativarPessoaJuridica(@RequestBody PessoaJuridica pessoaJuridica) throws LudiBoxException {
        service.desativarPessoaJuridica(pessoaJuridica);
    }
    
    @PutMapping("/reativar")
    public void reativarPessoaJuridica(@RequestBody PessoaJuridica pessoaJuridica) throws LudiBoxException {
        service.reativarPessoaJuridica(pessoaJuridica);
    }
    
    @PutMapping("/bloquear")
    public void bloquearPessoaJuridica(@RequestBody PessoaJuridica pessoaJuridica) throws LudiBoxException {
        service.bloquearPessoaJuridica(pessoaJuridica);
    }
    
    @GetMapping
    public List<PessoaJuridica> buscarTodosPj(){
    	List<PessoaJuridica> pessoas = service.buscarTodosPj();
    	return pessoas;
    }
    
}

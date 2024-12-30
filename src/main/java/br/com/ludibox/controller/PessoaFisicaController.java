package br.com.ludibox.controller;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.PessoaFisica;
import br.com.ludibox.service.PessoaFisicaService;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pessoa-fisica")
public class PessoaFisicaController {

    @Autowired
    private PessoaFisicaService service;

    @PostMapping
    public ResponseEntity<PessoaFisica> salvar(@RequestBody PessoaFisica pessoaFisica) throws LudiBoxException {
        return ResponseEntity.ok(service.salvar(pessoaFisica));
    }
    
    @PutMapping
    public ResponseEntity<PessoaFisica> atualizarPf(@RequestBody PessoaFisica pessoaFisica) throws LudiBoxException {
        return ResponseEntity.ok(service.atualizarDados(pessoaFisica));
    }
    
    @GetMapping
	public List<PessoaFisica> buscarTodosPf(){
		List<PessoaFisica> pessoas = service.buscarTodosPf();
		return pessoas;
	}
    
}

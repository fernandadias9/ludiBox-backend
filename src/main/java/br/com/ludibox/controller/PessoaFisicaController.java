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
    
    @PutMapping("/atualizar")
    public ResponseEntity<PessoaFisica> atualizarPf(@RequestBody PessoaFisica pessoaFisica) throws LudiBoxException {
        return ResponseEntity.ok(service.atualizarDados(pessoaFisica));
    }
    
    @PutMapping("/desativar")
    public void desativarPessoaFisica(@RequestBody PessoaFisica pessoaFisica) throws LudiBoxException {
        service.desativarPessoaFisica(pessoaFisica);
    }
    
    @PutMapping("/reativar")
    public void reativarPessoaFisica(@RequestBody PessoaFisica pessoaFisica) throws LudiBoxException {
        service.reativarPessoaFisica(pessoaFisica);
    }
    
    @PutMapping("/bloquear")
    public void bloquearPessoaFisica(@RequestBody PessoaFisica pessoaFisica) throws LudiBoxException {
        service.bloquearPessoaFisica(pessoaFisica);
    }
    
    @GetMapping
	public List<PessoaFisica> buscarTodosPf() throws LudiBoxException{
		List<PessoaFisica> pessoas = service.buscarTodosPf();
		return pessoas;
	}
    
}

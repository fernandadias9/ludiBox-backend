package br.com.ludibox.controller;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.PessoaFisica;
import br.com.ludibox.model.entity.PessoaJuridica;
import br.com.ludibox.service.PessoaJuridicaService;
import io.jsonwebtoken.io.IOException;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/pessoa-juridica")
public class PessoaJuridicaController {

    @Autowired
    private PessoaJuridicaService service;

    @Autowired
    private AuthenticationService authService;
    
    
    @PostMapping("/{idPessoa}/upload")
	public void UploadPessoa(@RequestParam("imagem") MultipartFile imagem, @PathVariable Integer idPessoa)
			throws LudiBoxException, IOException {
		if(imagem == null) {
			throw new LudiBoxException("Arquivo inválido");
		}
		
		PessoaFisica pessoaAutenticada = authService.getPessoaFisicaAutenticada();
		if (pessoaAutenticada == null) {
			throw new LudiBoxException("Usuário sem permissão de acesso");
		}
		
		service.salvarImagemPessoa(imagem, idPessoa);
  	
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
    public List<PessoaJuridica> buscarTodosPj() throws LudiBoxException{
    	List<PessoaJuridica> pessoas = service.buscarTodosPj();
    	return pessoas;
    }
    
}

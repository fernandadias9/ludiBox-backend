package br.com.ludibox.controller;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.PessoaFisica;
import br.com.ludibox.service.PessoaFisicaService;
import io.jsonwebtoken.io.IOException;
import jakarta.persistence.criteria.CriteriaBuilder.In;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/pessoa-fisica")
public class PessoaFisicaController {

    @Autowired
    private PessoaFisicaService service;
    
    @Autowired 
    private AuthenticationService authService;
    
    
    @PostMapping("/{idPessoa}/upload")
	public void UploadPessoa(@RequestParam("imagem") MultipartFile imagem, @PathVariable Integer idPessoa)
			throws LudiBoxException, IOException {
		if(imagem == null) {
			throw new LudiBoxException("Arquivo inválido", HttpStatus.BAD_REQUEST);
		}
		
		PessoaFisica pessoaAutenticada = authService.getPessoaFisicaAutenticada();
		if (pessoaAutenticada == null) {
			throw new LudiBoxException("Usuário sem permissão de acesso", HttpStatus.UNAUTHORIZED);
		}
		
		service.salvarImagemPessoa(imagem, idPessoa);
  	
	}    
    
    @PostMapping("/cadastrar_adm")
    public ResponseEntity<PessoaFisica> cadastrarAdm(@RequestBody  PessoaFisica pessoaFisica) throws LudiBoxException{
    	return ResponseEntity.ok(service.cadastrarAdm(pessoaFisica));
    }
    
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

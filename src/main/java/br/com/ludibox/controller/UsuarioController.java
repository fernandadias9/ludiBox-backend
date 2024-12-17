package br.com.ludibox.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.PessoaFisica;
import br.com.ludibox.model.entity.PessoaJuridica;
import br.com.ludibox.model.entity.Usuario;
import br.com.ludibox.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/api/usuarios")
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:5500" }, maxAge = 3600)
public class UsuarioController {

	@Autowired
	private UsuarioService usuarioService;

	@Operation(summary = "Cadastrar novo Usuário", description = "Adiciona um novo usuário ao sistema.", responses = {

			@ApiResponse(responseCode = "201", description = "Usuário criado com sucesso", content = 
					@Content(mediaType = "application/json", schema = 
					@Schema(implementation = Usuario.class))),

			@ApiResponse(responseCode = "400", description = "Erro de validação ou regra de negócio", content = 
					@Content(mediaType = "application/json", examples = 
					@ExampleObject(value = "{\"message\": \"Erro de validação: campo X é obrigatório\", \"status\": 400}"))) })

	@PostMapping
	public ResponseEntity<Usuario> inserirUsuario(@Valid @RequestBody Usuario novoUsuario) throws LudiBoxException {
		Usuario usuarioSalvo = usuarioService.inserirUsuario(novoUsuario);
		return new ResponseEntity(usuarioSalvo, HttpStatus.CREATED);
	}
	
	@Operation(summary = "Cadastrar nova Pessoa Física", description = "Adiciona uma pessoa física ao sistema.", responses = {

			@ApiResponse(responseCode = "201", description = "Pessoa física criada com sucesso", content = 
					@Content(mediaType = "application/json", schema = 
					@Schema(implementation = PessoaFisica.class))),

			@ApiResponse(responseCode = "400", description = "Erro de validação ou regra de negócio", content = 
					@Content(mediaType = "application/json", examples = 
					@ExampleObject(value = "{\"message\": \"Erro de validação: campo X é obrigatório\", \"status\": 400}"))) })

	@PostMapping("/pessoa_fisica")
	public ResponseEntity<PessoaFisica> inserirPessoaFisica(@Valid @RequestBody PessoaFisica novo) throws LudiBoxException {
		PessoaFisica usuarioSalvo = usuarioService.inserirPessoaFisica(novo);
		return new ResponseEntity(usuarioSalvo, HttpStatus.CREATED);
	}
	
	@Operation(summary = "Cadastrar nova Pessoa Jurídica", description = "Adiciona uma pessoa jurídica ao sistema.", responses = {

			@ApiResponse(responseCode = "201", description = "Pessoa jurídica criada com sucesso", content = 
					@Content(mediaType = "application/json", schema = 
					@Schema(implementation = PessoaJuridica.class))),

			@ApiResponse(responseCode = "400", description = "Erro de validação ou regra de negócio", content = 
					@Content(mediaType = "application/json", examples = 
					@ExampleObject(value = "{\"message\": \"Erro de validação: campo X é obrigatório\", \"status\": 400}"))) })

	@PostMapping("/pessoa_juridica")
	public ResponseEntity<PessoaJuridica> inserirPessoaJuridica(@Valid @RequestBody PessoaJuridica novo) throws LudiBoxException {
		PessoaJuridica usuarioSalvo = usuarioService.inserirPessoaJuridica(novo);
		return new ResponseEntity(usuarioSalvo, HttpStatus.CREATED);
	}
	
	

}

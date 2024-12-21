package br.com.ludibox.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Endereco;
import br.com.ludibox.model.entity.Usuario;
import br.com.ludibox.service.EnderecoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/api/enderecos")
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:5500" }, maxAge = 3600)
public class EnderecoController {
	
	private EnderecoService enderecoService;
	
	
	@Operation(summary = "Cadastrar novo Endereço", description = "Adiciona um novo Endereço ao sistema.", responses = {

			@ApiResponse(responseCode = "201", description = "Endereço criado com sucesso", content = 
					@Content(mediaType = "application/json", schema = 
					@Schema(implementation = Endereco.class))),

			@ApiResponse(responseCode = "400", description = "Erro de validação ou regra de negócio", content = 
					@Content(mediaType = "application/json", examples = 
					@ExampleObject(value = "{\"message\": \"Erro de validação: campo X é obrigatório\", \"status\": 400}"))) })

	@PostMapping()
	public ResponseEntity<Endereco> inserirEndereco(@Valid @RequestBody Endereco novoEndereco) throws LudiBoxException {
		Endereco enderecoSalvo = enderecoService.inserirEndereco(novoEndereco);
		return new ResponseEntity(enderecoSalvo, HttpStatus.CREATED);
	}
	

}

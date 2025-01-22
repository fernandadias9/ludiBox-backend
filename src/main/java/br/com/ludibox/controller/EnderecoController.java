package br.com.ludibox.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Endereco;
import br.com.ludibox.service.EnderecoService;


@RestController
@RequestMapping("/endereco")
public class EnderecoController {
	
	@Autowired
	private EnderecoService enderecoService;
	
	@PostMapping("/pessoaFisica")
	public ResponseEntity<Endereco> salvarEnderecoParaPf(@RequestBody Endereco novo) throws LudiBoxException{
		return ResponseEntity.ok(enderecoService.salvarEnderecoParaPf(novo));
	}
	
	@PostMapping("/pessoaJuridica")
	public ResponseEntity<Endereco> salvarEnderecoParaPj(@RequestBody Endereco novo) throws LudiBoxException{
		return ResponseEntity.ok(enderecoService.salvarEnderecoParaPj(novo));
	}
	
	

}

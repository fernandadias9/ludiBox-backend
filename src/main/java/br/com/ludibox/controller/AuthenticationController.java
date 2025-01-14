package br.com.ludibox.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.PessoaFisica;
import br.com.ludibox.model.entity.PessoaJuridica;
import br.com.ludibox.model.enums.EnumPerfil;
import br.com.ludibox.service.PessoaFisicaService;
import br.com.ludibox.service.PessoaJuridicaService;
import jakarta.validation.Valid;


@RestController
@RequestMapping(path = "/auth")
public class AuthenticationController {
	
	@Autowired
	private AuthenticationService authenticationService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private PessoaFisicaService pessoaFisicaService;
	
	@Autowired
	private PessoaJuridicaService pessoaJuridicaService;
	
	@PostMapping("authenticatePf")
	public String authenticatePf(Authentication authentication) throws LudiBoxException {
		return authenticationService.authenticatePessoaFisica(authentication);
	}
	
	@PostMapping("authenticatePj")
	public String authenticatePj(Authentication authentication) throws LudiBoxException {
		return authenticationService.authenticatePessoaJuridica(authentication);
	}
	
	 @PostMapping("/cadastrar_adm")
	 @ResponseStatus(code = HttpStatus.CREATED)
	    public ResponseEntity<PessoaFisica> cadastrarAdm(@RequestBody @Valid PessoaFisica pessoaFisica) throws LudiBoxException{
		 	String senhaCifrada = passwordEncoder.encode(pessoaFisica.getSenha());
			pessoaFisica.setSenha(senhaCifrada);
		 return ResponseEntity.ok(pessoaFisicaService.cadastrarAdm(pessoaFisica));
	    }
	
	@PostMapping("/nova-pessoa-fisica")
	@ResponseStatus(code = HttpStatus.CREATED) 
	public void registrarPessoaFisica(@RequestBody @Valid PessoaFisica novaPessoaFisica) throws LudiBoxException {
		String senhaCifrada = passwordEncoder.encode(novaPessoaFisica.getSenha());
		novaPessoaFisica.setSenha(senhaCifrada);
		novaPessoaFisica.setPerfil(EnumPerfil.USUARIO);
		
		pessoaFisicaService.salvar(novaPessoaFisica); 
	} 
	
	@PostMapping("/nova-pessoa-juridica")
	@ResponseStatus(code = HttpStatus.CREATED) 
	public void registrarPessoaJuridica(@RequestBody @Valid PessoaJuridica novaPessoaJuridica) throws LudiBoxException {
		String senhaCifrada = passwordEncoder.encode(novaPessoaJuridica.getSenha());
		novaPessoaJuridica.setSenha(senhaCifrada);
		novaPessoaJuridica.setPerfil(EnumPerfil.USUARIO);
		
		pessoaJuridicaService.salvar(novaPessoaJuridica); 
	} 
	
 }
 
	

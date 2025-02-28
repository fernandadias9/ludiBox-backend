package br.com.ludibox.controller;

import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.service.PessoaService;
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
import br.com.ludibox.model.enums.EnumPerfil;
import jakarta.validation.Valid;


@RestController
@RequestMapping(path = "/auth")
public class AuthenticationController {

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private PessoaService pessoaService;

	@PostMapping("authenticatePessoa")
	public String authenticatePessoa(Authentication authentication) throws LudiBoxException {
		return authenticationService.authenticatePessoa(authentication);
	}


	@PostMapping("/cadastrar_adm")
	@ResponseStatus(code = HttpStatus.CREATED)
	public ResponseEntity<Pessoa> cadastrarAdm(@RequestBody @Valid Pessoa pessoa) throws LudiBoxException {
		String senhaCifrada = passwordEncoder.encode(pessoa.getSenha());
		pessoa.setSenha(senhaCifrada);
		return ResponseEntity.ok(pessoaService.cadastrarAdm(pessoa));
	}

	@PostMapping("/nova-pessoa")
	@ResponseStatus(code = HttpStatus.CREATED)
	public void registrarPessoa(@RequestBody @Valid Pessoa novaPessoa) throws LudiBoxException {
		String senhaCifrada = passwordEncoder.encode(novaPessoa.getSenha());
		novaPessoa.setSenha(senhaCifrada);
			novaPessoa.setPerfil(EnumPerfil.USUARIO);

		pessoaService.salvar(novaPessoa);

	}
}
 
	

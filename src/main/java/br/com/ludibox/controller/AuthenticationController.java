package br.com.ludibox.controller;

import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.enums.EnumStatus;
import br.com.ludibox.service.GoogleAuthenticatorService;
import br.com.ludibox.service.PessoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.enums.EnumPerfil;
import jakarta.validation.Valid;

import java.util.Date;

@RestController
@RequestMapping(path = "/auth")
public class AuthenticationController {

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private PessoaService pessoaService;

	@Autowired
	private GoogleAuthenticatorService googleAuthenticatorService;


	@PostMapping("/authenticatePessoa")
	public String authenticatePessoa(
			Authentication authentication,
			@RequestParam(value = "code", required = false) String codeFromUser
	) throws LudiBoxException {

		Pessoa pessoa = pessoaService.buscarPorEmail(authentication.getName());

		if (pessoa.isTwoFactorEnabled() && pessoa.isTwoFactorConfirmed()) {
			if (codeFromUser == null || codeFromUser.isEmpty()) {
				throw new LudiBoxException("Erro", "Código TOTP é obrigatório.", HttpStatus.UNAUTHORIZED);
			}

			boolean isValid = googleAuthenticatorService.isCodeValid(pessoa.getSecretTotp(), codeFromUser);

			if (!isValid) {
				throw new LudiBoxException("Erro", "Código TOTP inválido.", HttpStatus.UNAUTHORIZED);
			}
		}

		return authenticationService.authenticatePessoa(authentication);
	}



	@PostMapping("/cadastrar_adm")
	@ResponseStatus(code = HttpStatus.CREATED)
	public ResponseEntity<Pessoa> cadastrarAdm(@RequestBody @Valid Pessoa pessoa) throws LudiBoxException {
		return ResponseEntity.ok(pessoaService.cadastrarAdm(pessoa));
	}

	@PostMapping("/nova-pessoa")
	@ResponseStatus(code = HttpStatus.CREATED)
	public void registrarPessoa(@RequestBody @Valid Pessoa novaPessoa) throws LudiBoxException {
		pessoaService.salvar(novaPessoa);

	}
}
 
	

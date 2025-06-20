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
import java.util.Map;

@RestController
@RequestMapping(path = "/auth")
public class AuthenticationController {

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private PessoaService pessoaService;

	@Autowired
	private GoogleAuthenticatorService googleAuthenticatorService;


	@PostMapping("/login")
	public ResponseEntity<?> login(
			@RequestParam String email,
			@RequestParam String senha
	) throws LudiBoxException {
		Pessoa pessoa = pessoaService.buscarPorEmail(email);

		if (!pessoaService.validarSenha(senha, pessoa)) {
			throw new LudiBoxException("Erro", "Credenciais inválidas", HttpStatus.UNAUTHORIZED);
		}

		if (pessoa.isTwoFactorEnabled() && pessoa.isTwoFactorConfirmed()) {
			String tokenTemporario = authenticationService.gerarTokenTemporario(email);
			return ResponseEntity.ok().body(
					Map.of("twoFactorRequired", true, "tempToken", tokenTemporario)
			);
		}

		// Senha válida e sem 2FA: gera JWT direto
		Authentication authentication = authenticationService.autenticarComEmail(email);
		String jwt = authenticationService.authenticatePessoa(authentication);
		return ResponseEntity.ok().body(Map.of("jwt", jwt));
	}

	@PostMapping("/2fa/confirm")
	public ResponseEntity<?> confirmarTotp(
			@RequestParam String tempToken,
			@RequestParam String code
	) throws LudiBoxException {

		String email = authenticationService.validarTokenTemporario(tempToken);
		Pessoa pessoa = pessoaService.buscarPorEmail(email);

		if (!pessoa.isTwoFactorEnabled() || !pessoa.isTwoFactorConfirmed()) {
			throw new LudiBoxException("Erro", "2FA não está habilitado para essa conta", HttpStatus.BAD_REQUEST);
		}

		boolean isValid = googleAuthenticatorService.isCodeValid(pessoa.getSecretTotp(), code);
		if (!isValid) {
			throw new LudiBoxException("Erro", "Código TOTP inválido ou expirado", HttpStatus.UNAUTHORIZED);
		}

		// Aqui você deve autenticar o usuário com Spring Security
		Authentication authentication = authenticationService.autenticarComEmail(pessoa.getEmail());
		String jwt = authenticationService.authenticatePessoa(authentication);

		return ResponseEntity.ok().body(Map.of("jwt", jwt));
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
 
	

package br.com.ludibox.controller;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.dto.AuthResponseDTO;
import br.com.ludibox.model.dto.LoginRequestDTO;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.service.GoogleAuthenticatorService;
import br.com.ludibox.service.PessoaService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(path = "/auth")
public class AuthenticationController {

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private PessoaService pessoaService;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private GoogleAuthenticatorService googleAuthenticatorService;

	/**
	 * Login unificado com suporte a 2FA via TOTP.
	 * Se 2FA estiver ativo, retorna um token temporário e sinaliza necessidade do código.
	 * Caso contrário, retorna JWT direto.
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDTO request) throws LudiBoxException {
		Pessoa pessoa = pessoaService.buscarPorEmail(request.getUsername());

		if (!pessoaService.validarSenha(request.getPassword(), pessoa)) {
			throw new LudiBoxException("Erro", "Credenciais inválidas", HttpStatus.UNAUTHORIZED);
		}

		// Se 2FA estiver habilitado e confirmado, retorna token temporário
		if (pessoa.isTwoFactorEnabled() && pessoa.isTwoFactorConfirmed()) {
			String tokenTemporario = authenticationService.gerarTokenTemporario(pessoa.getEmail());
			return ResponseEntity.ok().body(
					Map.of("twoFactorRequired", true, "tempToken", tokenTemporario)
			);
		}

		// Autenticação normal
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
		);
		String jwt = authenticationService.authenticatePessoa(authentication);
		return ResponseEntity.ok(new AuthResponseDTO(jwt));
	}

	/**
	 * Confirma o código TOTP e retorna JWT após validação bem-sucedida.
	 */
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

		Authentication authentication = authenticationService.autenticarComEmail(pessoa.getEmail());
		String jwt = authenticationService.authenticatePessoa(authentication);

		return ResponseEntity.ok(new AuthResponseDTO(jwt));
	}

	@PostMapping("/cadastrar_adm")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<Pessoa> cadastrarAdm(@RequestBody @Valid Pessoa pessoa) throws LudiBoxException {
		return ResponseEntity.ok(pessoaService.cadastrarAdm(pessoa));
	}

	@PostMapping("/nova-pessoa")
	@ResponseStatus(HttpStatus.CREATED)
	public void registrarPessoa(@RequestBody @Valid Pessoa novaPessoa) throws LudiBoxException {
		pessoaService.salvar(novaPessoa);
	}
}

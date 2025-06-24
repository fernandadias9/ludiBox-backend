package br.com.ludibox.auth;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.enums.EnumPerfil;
import br.com.ludibox.model.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

	private final JwtService jwtService;

	@Autowired
	private PessoaRepository pessoaRepository;

	public AuthenticationService(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	/**
	 * Gera o JWT principal para a pessoa autenticada
	 */
	public String authenticatePessoa(Authentication authentication) throws LudiBoxException {
		return jwtService.getGenerateTokenPessoa(authentication);
	}

	/**
	 * Gera um token temporário para 2FA
	 */
	public String gerarTokenTemporario(String email) {
		return jwtService.gerarTokenTemporario(email);
	}

	/**
	 * Valida o token temporário (2FA) e retorna o email contido nele
	 */
	public String validarTokenTemporario(String token) throws LudiBoxException {
		return jwtService.validarTokenTemporario(token);
	}

	/**
	 * Cria um Authentication (UsernamePasswordAuthenticationToken) com base no email da pessoa
	 */
	public Authentication autenticarComEmail(String email) {
		Pessoa pessoa = pessoaRepository.findByEmail(email)
				.orElseThrow(() -> new LudiBoxException("Erro: ", "Usuário não encontrado: " + email, HttpStatus.NOT_FOUND));
		return new UsernamePasswordAuthenticationToken(pessoa, null, pessoa.getAuthorities());
	}

	/**
	 * Recupera o objeto Pessoa do usuário atualmente autenticado com base no ID do JWT
	 */
	public Pessoa getPessoaAutenticada() throws LudiBoxException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			throw new LudiBoxException("Erro: ", "Usuário não autenticado.", HttpStatus.BAD_REQUEST);
		}

		Object principal = authentication.getPrincipal();

		if (principal instanceof Jwt jwt) {
			try {
				Integer id = Integer.valueOf(jwt.getSubject());
				return pessoaRepository.findById(id)
						.orElseThrow(() -> new LudiBoxException("Not found: ", "Usuário não encontrado com ID: " + id, HttpStatus.BAD_REQUEST));
			} catch (NumberFormatException e) {
				throw new LudiBoxException("Erro: ", "ID inválido no token JWT", HttpStatus.BAD_REQUEST);
			}
		} else if (principal instanceof Pessoa pessoa) {
			return pessoa;
		}

		throw new LudiBoxException("Erro: ", "Tipo inesperado de principal: " + principal.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Valida se o usuário autenticado tem perfil de administrador
	 */
	public void verificarPermissaoAdmin() {
		Pessoa pessoaAutenticada = getPessoaAutenticada();
		if (pessoaAutenticada.getPerfil() == EnumPerfil.USUARIO) {
			throw new LudiBoxException("Administração: ", "Ação exclusiva para administradores!", HttpStatus.UNAUTHORIZED);
		}
	}
}

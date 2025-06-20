package br.com.ludibox.auth;

import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.enums.EnumPerfil;
import br.com.ludibox.model.repository.PessoaRepository;
import br.com.ludibox.service.PessoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import br.com.ludibox.exception.LudiBoxException;

@Service
public class AuthenticationService {
	
	private final JwtService jwtService;

	@Autowired
	private PessoaRepository pessoaRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;




	public String gerarTokenTemporario(String email) {
		return jwtService.gerarTokenTemporario(email);
	}

	public String validarTokenTemporario(String token) throws LudiBoxException {
		return jwtService.validarTokenTemporario(token);
	}

	public Authentication autenticarComEmail(String email) {
		Pessoa pessoa = pessoaRepository.findByEmail(email).get();
		return new UsernamePasswordAuthenticationToken(pessoa, null, pessoa.getAuthorities());
	}

	public AuthenticationService(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	public String authenticatePessoa(Authentication authentication) throws LudiBoxException{
		return jwtService.getGenerateTokenPessoa(authentication);
	}

	public Pessoa getPessoaAutenticada() throws LudiBoxException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


		if (authentication == null || !authentication.isAuthenticated()) {
			throw new LudiBoxException("Erro: ", "Usuário não autenticado.", HttpStatus.BAD_REQUEST);
		}

		Object principal = authentication.getPrincipal();


		if (principal instanceof Jwt) {
			Jwt jwt = (Jwt) principal;
			Integer id = Integer.valueOf(jwt.getSubject());

			return pessoaRepository.findById(id)
					.orElseThrow(() -> new LudiBoxException("Not found: ", "Usuário não encontrado com ID: " + id, HttpStatus.BAD_REQUEST));
		}


		throw new LudiBoxException("Erro: ", "Tipo inesperado de principal: " + principal.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);


	}

	public void verificarPermissaoAdmin() {
		Pessoa pessoaAutenticada = getPessoaAutenticada();

		if (pessoaAutenticada.getPerfil() == EnumPerfil.USUARIO) {
			throw new LudiBoxException("Administração: ", "Ação exclusiva para administradores!", HttpStatus.UNAUTHORIZED);
		}
	}
}

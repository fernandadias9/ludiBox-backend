package br.com.ludibox.auth;

import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.enums.EnumPerfil;
import br.com.ludibox.model.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import br.com.ludibox.exception.LudiBoxException;

@Service
public class AuthenticationService {
	
	private final JwtService jwtService;

	@Autowired
	private PessoaRepository pessoaRepository;
	
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

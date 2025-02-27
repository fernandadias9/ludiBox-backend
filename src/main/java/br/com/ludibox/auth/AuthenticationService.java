package br.com.ludibox.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.PessoaFisica;
import br.com.ludibox.model.entity.PessoaJuridica;
import br.com.ludibox.model.repository.PessoaFisicaRepository;
import br.com.ludibox.model.repository.PessoaJuridicaRepository;

@Service
public class AuthenticationService {
	
	private final JwtService jwtService;
	
	@Autowired
	private PessoaFisicaRepository pessoaFisicaRepository;
	
	@Autowired
	private PessoaJuridicaRepository pessoaJuridicaRepository;
	
	public AuthenticationService(JwtService jwtService) {
		this.jwtService = jwtService;
	}
	
	public String authenticatePessoaFisica(Authentication authentication) throws LudiBoxException {
		return jwtService.getGenerateTokenPessoaFisica(authentication);
	}
	
	public String authenticatePessoaJuridica(Authentication authentication) throws LudiBoxException {
		return jwtService.getGenerateTokenPessoaJuridica(authentication);
	}
	
	 public PessoaFisica getPessoaFisicaAutenticada() throws LudiBoxException {
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        
	        
	        if (authentication == null || !authentication.isAuthenticated()) {
	            throw new LudiBoxException("Usuário não autenticado.", HttpStatus.BAD_REQUEST);
	        }
	        
	        Object principal = authentication.getPrincipal();

	        
	        if (principal instanceof Jwt) {
	            Jwt jwt = (Jwt) principal;
	            String login = jwt.getClaim("sub");

	            return pessoaFisicaRepository.findByEmail(login)
	                    .orElseThrow(() -> new LudiBoxException("Usuário não encontrado: " + login, HttpStatus.BAD_REQUEST));
	        } else if (principal instanceof UserDetails) {
	            String username = ((UserDetails) principal).getUsername();

	            return pessoaFisicaRepository.findByEmail(username)
	                    .orElseThrow(() -> new LudiBoxException("Usuário não encontrado: " + username, HttpStatus.BAD_REQUEST));
	        } else if (principal instanceof PessoaFisica) {
	            return (PessoaFisica) principal;
	        }
	        
	        throw new LudiBoxException("Tipo inesperado de principal: " + principal.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
	        
	    
	    }
	 
	 public PessoaJuridica getPessoaJuridicaAutenticada() throws LudiBoxException {
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        
	        
	        if (authentication == null || !authentication.isAuthenticated()) {
	            throw new LudiBoxException("Usuário não autenticado.", HttpStatus.BAD_REQUEST);
	        }
	        
	        Object principal = authentication.getPrincipal();

	        
	        if (principal instanceof Jwt) {
	            Jwt jwt = (Jwt) principal;
	            String login = jwt.getClaim("sub");

	            return pessoaJuridicaRepository.findByEmail(login)
	                    .orElseThrow(() -> new LudiBoxException("Usuário não encontrado: " + login, HttpStatus.INTERNAL_SERVER_ERROR));
	        } else if (principal instanceof UserDetails) {
	            String username = ((UserDetails) principal).getUsername();

	            return pessoaJuridicaRepository.findByEmail(username)
	                    .orElseThrow(() -> new LudiBoxException("Usuário não encontrado: " + username, HttpStatus.INTERNAL_SERVER_ERROR));
	        } else if (principal instanceof PessoaFisica) {
	            return (PessoaJuridica) principal;
	        }
	        
	        throw new LudiBoxException("Tipo inesperado de principal: " + principal.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
	        
	    
	    }
}

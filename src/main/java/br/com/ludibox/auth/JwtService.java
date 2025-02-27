package br.com.ludibox.auth;

import java.security.Principal;
import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.PessoaFisica;
import br.com.ludibox.model.entity.PessoaJuridica;
import br.com.ludibox.model.entity.Usuario;
import br.com.ludibox.model.repository.PessoaFisicaRepository;
import br.com.ludibox.model.repository.PessoaJuridicaRepository;


@Service
public class JwtService {
	private final JwtEncoder jwtEncoder;

	@Autowired
	PessoaFisicaRepository pessoaFisicaRepository;
	
	@Autowired
	PessoaJuridicaRepository pessoaJuridicaRepository;

	public JwtService(JwtEncoder jwtEncoder) {
		this.jwtEncoder = jwtEncoder;
	}

	public String getGenerateTokenPessoaFisica(Authentication authentication) throws LudiBoxException {
		Instant now = Instant.now();

		long dezHorasEmSegundo = 36000L;

		String rles = authentication
				.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(" "));

		Object principal = authentication.getPrincipal();

		PessoaFisica pessoaFisicaAutenticada;

		if (principal instanceof Jwt) {
			Jwt jwt = (Jwt) principal;
			String login = jwt.getClaim("sub");

			pessoaFisicaAutenticada = pessoaFisicaRepository.findByEmail(login)
					.orElseThrow(() -> new LudiBoxException("Usuário não encontrado: " + login, HttpStatus.BAD_REQUEST));
		}else {

			pessoaFisicaAutenticada = (PessoaFisica) authentication.getPrincipal();

		}
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("ludibox")
				.issuedAt(now)
				.expiresAt(now.plusSeconds(dezHorasEmSegundo))
				.subject(authentication.getName())
				.claim("roles", rles)
				.claim("id", pessoaFisicaAutenticada.getId())
				.build();

		return jwtEncoder.encode(
				JwtEncoderParameters.from(claims)).getTokenValue();

	}

	
	public String getGenerateTokenPessoaJuridica(Authentication authentication) throws LudiBoxException {
		Instant now = Instant.now();

		long dezHorasEmSegundo = 36000L;

		String rles = authentication
				.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(" "));

		Object principal = authentication.getPrincipal();

		PessoaJuridica pessoaJuridicaAutenticada;

		if (principal instanceof Jwt) {
			Jwt jwt = (Jwt) principal;
			String login = jwt.getClaim("sub");

			pessoaJuridicaAutenticada = pessoaJuridicaRepository.findByEmail(login)
					.orElseThrow(() -> new LudiBoxException("Usuário não encontrado: " + login, HttpStatus.BAD_REQUEST));
		}else {

			pessoaJuridicaAutenticada = (PessoaJuridica) authentication.getPrincipal();

		}
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("ludibox")
				.issuedAt(now)
				.expiresAt(now.plusSeconds(dezHorasEmSegundo))
				.subject(authentication.getName())
				.claim("roles", rles)
				.claim("id", pessoaJuridicaAutenticada.getId())
				.build();

		return jwtEncoder.encode(
				JwtEncoderParameters.from(claims)).getTokenValue();

	}



	

}

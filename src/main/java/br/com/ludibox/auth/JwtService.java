package br.com.ludibox.auth;

import java.time.Instant;
import java.util.stream.Collectors;

import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.repository.PessoaRepository;
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


@Service
public class JwtService {
	private final JwtEncoder jwtEncoder;

	@Autowired
	PessoaRepository pessoaRepository;

	public JwtService(JwtEncoder jwtEncoder) {
		this.jwtEncoder = jwtEncoder;
	}

	public String getGenerateTokenPessoa(Authentication authentication) throws LudiBoxException {
		Instant now = Instant.now();

		long dezHorasEmSegundo = 36000L;

		String rles = authentication
				.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(" "));

		Object principal = authentication.getPrincipal();

		Pessoa pessoaAutenticada;

		if (principal instanceof Jwt) {
			Jwt jwt = (Jwt) principal;
			String login = jwt.getClaim("sub");

			pessoaAutenticada = pessoaRepository.findByEmail(login)
					.orElseThrow(() -> new LudiBoxException("Not Found: ", "Usuário não encontrado: " + login, HttpStatus.BAD_REQUEST));
		}else {

			pessoaAutenticada = (Pessoa) authentication.getPrincipal();

		}
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("ludibox")
				.issuedAt(now)
				.expiresAt(now.plusSeconds(dezHorasEmSegundo))
				.subject(authentication.getName())
				.claim("roles", rles)
				.claim("id", pessoaAutenticada.getId())
				.build();

		return jwtEncoder.encode(
				JwtEncoderParameters.from(claims)).getTokenValue();

	}

	

}

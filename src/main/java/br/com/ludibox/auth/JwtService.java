package br.com.ludibox.auth;

import java.time.Instant;
import java.util.stream.Collectors;

import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import br.com.ludibox.exception.LudiBoxException;


@Service
public class JwtService {
	private final JwtEncoder jwtEncoder;
	private final JwtDecoder jwtDecoder;

	@Autowired
	PessoaRepository pessoaRepository;

	public JwtService(@Lazy JwtEncoder jwtEncoder, @Lazy JwtDecoder jwtDecoder) {
		this.jwtEncoder = jwtEncoder;
		this.jwtDecoder = jwtDecoder;
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
				.subject(String.valueOf(pessoaAutenticada.getId()))
				.claim("roles", rles)
				.claim("id", pessoaAutenticada.getId())
				.build();


		return jwtEncoder.encode(
				JwtEncoderParameters.from(claims)).getTokenValue();

	}

	public String gerarTokenTemporario(String email) {
		Pessoa pessoa = pessoaRepository.findByEmail(email)
				.orElseThrow(() -> new LudiBoxException("Erro", "Usuário não encontrado", HttpStatus.BAD_REQUEST));


		Instant now = Instant.now();
		long validadeCurtaSegundos = 300L; // 5 minutos

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("ludibox")
				.issuedAt(now)
				.expiresAt(now.plusSeconds(validadeCurtaSegundos))
				.subject(email)
				.claim("tipo", "2fa_temp")
				.build();

		return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
	}

	public String validarTokenTemporario(String token) throws LudiBoxException {
		try {
			Jwt jwt = jwtDecoder.decode(token);

			if (!"2fa_temp".equals(jwt.getClaimAsString("tipo"))) {
				throw new LudiBoxException("Erro", "Token inválido para 2FA", HttpStatus.UNAUTHORIZED);
			}

			return jwt.getSubject(); // que é o email
		} catch (JwtException e) {
			throw new LudiBoxException("Erro", "Token expirado ou inválido", HttpStatus.UNAUTHORIZED);
		}
	}





}

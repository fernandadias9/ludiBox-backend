package br.com.ludibox.auth;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Value("${jwt.public.key}")
	private RSAPublicKey publicKey;

	@Value("${jwt.private.key}")
	private RSAPrivateKey privateKey;

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		//Stateless -> não guarda o estado da aplicação (padrão usado no REST)
		//Stateful -> guarda o estado da aplicação
		//https://medium.com/exactaworks/stateless-vs-stateful-f596a6b6471d
		http
		.cors(cors -> cors.configurationSource(corsConfigurationSource()))
		.csrf(csrf -> csrf.disable())
		.anonymous(Customizer.withDefaults())
		.authorizeHttpRequests(
				//Hierarquia de permissões e bloqueios
				auth -> auth
				//URLs liberadas
				.requestMatchers("/auth/**", "/public/**",  "/produto/**", "/api/password/reset", "/avaliacoes/produto/**").permitAll()
				.requestMatchers(produtoListarComFiltroMatcher()).permitAll()
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				//Todas as demais são bloqueadas
				.anyRequest().authenticated())
		.httpBasic(Customizer.withDefaults())
		.oauth2ResourceServer(
				conf -> conf.jwt(Customizer.withDefaults()));

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		// Use setAllowedOriginPatterns com as duas URLs corretas, sem "/*" no final
		configuration.setAllowedOriginPatterns(List.of(
				"http://localhost:4200",
				"https://ludibox-frontend.onrender.com"
		));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
		// Seja explícito sobre os cabeçalhos que você permite e expõe
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setExposedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	JwtDecoder jwtDecoder() {
		return NimbusJwtDecoder.withPublicKey(publicKey).build();
	}

	@Bean
	JwtEncoder jwtEncoder(){
		JWK jwk = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
		JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
		return  new NimbusJwtEncoder(jwks);
	}

	@Bean
	PasswordEncoder passwordEncoder(){
		return new RSAPasswordEncoder(publicKey, privateKey);
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public RequestMatcher produtoListarComFiltroMatcher() {
		return new RequestMatcher() {
			@Override
			public boolean matches(HttpServletRequest request) {
				String path = request.getRequestURI();
				String method = request.getMethod();

				return "GET".equals(method) &&
						path.equals("/produto/listarComFiltro");
			}
		};
	}

}

package br.com.ludibox.controller;

import br.com.ludibox.model.dto.AuthResponseDTO;
import br.com.ludibox.model.dto.LoginRequestDTO;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.service.PessoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import jakarta.validation.Valid;


@RestController
@RequestMapping(path = "/auth")
public class AuthenticationController {

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private PessoaService pessoaService;
    @Autowired
    private AuthenticationManager authenticationManager;

	@PostMapping("authenticatePessoa")
	public AuthResponseDTO authenticatePessoa(@RequestBody LoginRequestDTO request) throws LudiBoxException {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
		);
		String token = authenticationService.authenticatePessoa(authentication);
		return new AuthResponseDTO(token);
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
 
	

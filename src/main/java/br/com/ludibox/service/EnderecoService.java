package br.com.ludibox.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Endereco;
import br.com.ludibox.model.entity.PessoaFisica;
import br.com.ludibox.model.entity.PessoaJuridica;
import br.com.ludibox.model.repository.EnderecoRepository;
import br.com.ludibox.model.repository.PessoaFisicaRepository;
import br.com.ludibox.model.repository.PessoaJuridicaRepository;

@Service
public class EnderecoService {
	
	@Autowired
	private EnderecoRepository enderecoRepository;
	
	@Autowired
	private PessoaFisicaRepository pessoaFisicaRepository;
	
	@Autowired
	private PessoaJuridicaRepository pessoaJuridicaRepository;
	
	 @Autowired
	private AuthenticationService authService;
	
	 
	public Endereco salvarEnderecoParaPf(Endereco novo) throws LudiBoxException {
			PessoaFisica pfAutenticada = authService.getPessoaFisicaAutenticada();
			
			validarPessoaFisica(pfAutenticada);
			validarNumeroDePf(novo);
			novo.setPessoaFisica(pfAutenticada);
			pfAutenticada.getEnderecos().add(novo);
			
			return enderecoRepository.save(novo);
		}
	 
	public Endereco salvarEnderecoParaPj(Endereco novo) throws LudiBoxException {
		PessoaJuridica pjAutenticada = authService.getPessoaJuridicaAutenticada();
		Endereco enderecoValidado = new Endereco();

		validarPessoaJuridica(pjAutenticada);
		validarNumeroDePj(novo);
		
		enderecoValidado = pessoaJaPossuiEndereco(novo);
		
		enderecoValidado.setPessoaJuridica(pjAutenticada);
		pjAutenticada.setEndereco(enderecoValidado);
	
		return enderecoRepository.save(enderecoValidado);
	}
	
	public Endereco atualizarEnderecoPf(Endereco endereco) throws LudiBoxException{
    	PessoaFisica pessoaAutenticada = authService.getPessoaFisicaAutenticada();   	
    	validarPessoaFisica(pessoaAutenticada);
		validarNumeroDePf(endereco);
    	if (pessoaAutenticada.getId() != endereco.getPessoaFisica().getId()) {
			throw new LudiBoxException("Usuários só podem alterar seus próprios dados!");
		}
    	return enderecoRepository.save(endereco);
    }
	
	public Endereco atualizarEnderecoPj(Endereco endereco) throws LudiBoxException{
    	PessoaJuridica pessoaAutenticada = authService.getPessoaJuridicaAutenticada();   	
    	validarPessoaJuridica(pessoaAutenticada);
		validarNumeroDePj(endereco);
    	if (pessoaAutenticada.getId() != endereco.getPessoaJuridica().getId()) {
			throw new LudiBoxException("Usuários só podem alterar seus próprios dados!");
		}
    	return enderecoRepository.save(endereco);
    }
	
	private Endereco pessoaJaPossuiEndereco(Endereco novo) throws LudiBoxException {
		PessoaJuridica pjAutenticada = authService.getPessoaJuridicaAutenticada();
		Endereco existente = pjAutenticada.getEndereco();

		if(existente != null) {
			novo.setId(existente.getId());
		}
		return novo;
	}

	private void validarPessoaFisica(PessoaFisica pessoaValidada) throws LudiBoxException {
	    pessoaFisicaRepository.findById(pessoaValidada.getId())
	        .orElseThrow(() -> new LudiBoxException("Pessoa física com ID " + pessoaValidada.getId() + " não encontrada!"));
	}

	private void validarPessoaJuridica(PessoaJuridica pessoaValidada) throws LudiBoxException {
	    pessoaJuridicaRepository.findById(pessoaValidada.getId())
	        .orElseThrow(() -> new LudiBoxException("Pessoa jurídica com ID " + pessoaValidada.getId() + " não encontrada!"));
	}

	public void validarNumeroDePf(Endereco enderecoValidado) throws LudiBoxException {
	    Integer numero = enderecoValidado.getNumero();
	    if (enderecoValidado.isSemNumero() && numero != null && numero != 0) {
	        throw new LudiBoxException("Número não foi solicitado!");
	    } else if (!enderecoValidado.isSemNumero() && (numero == null || numero == 0)) {
	        throw new LudiBoxException("Número não foi adicionado!");
	    }
	}

	public void validarNumeroDePj(Endereco enderecoValidado) throws LudiBoxException {
	    Integer numero = enderecoValidado.getNumero();
	    if (numero == null || numero == 0) {
	        throw new LudiBoxException("Número não foi adicionado!");
	    }
	}

	

}

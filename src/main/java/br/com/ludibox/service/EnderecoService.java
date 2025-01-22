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

		validarPessoaJuridica(pjAutenticada);
		validarNumeroDePj(novo);
		
		pessoaJaPossuiEndereco(pjAutenticada);
		
		novo.setPessoaJuridica(pjAutenticada);
		pjAutenticada.setEndereco(novo);
	
		return enderecoRepository.save(novo);
	}
	
	private void pessoaJaPossuiEndereco(PessoaJuridica pjAutenticada) throws LudiBoxException {
		if(pjAutenticada.getEndereco() != null) {
			throw new LudiBoxException("Pessoa já possuí endereço cadastrado!");
		}
		
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

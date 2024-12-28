package br.com.ludibox.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

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
	
	public Endereco salvarEndereco(Endereco novo) throws LudiBoxException {
		validaPessoa(novo);
		
		return enderecoRepository.save(novo);
	}
	
	public void validaPessoa(Endereco enderecoValidado) throws LudiBoxException {
	    if (enderecoValidado.getPessoaFisica() != null && enderecoValidado.getPessoaJuridica() == null) {
	        validarPessoaFisica(enderecoValidado);
	    } else if (enderecoValidado.getPessoaJuridica() != null && enderecoValidado.getPessoaFisica() == null) {
	        validarPessoaJuridica(enderecoValidado);
	    } else {
	        throw new LudiBoxException("Endereço deve estar associado somente a uma pessoa física ou jurídica!");
	    }
	}

	private void validarPessoaFisica(Endereco enderecoValidado) throws LudiBoxException {
	    pessoaFisicaRepository.findById(enderecoValidado.getPessoaFisica().getId())
	        .orElseThrow(() -> new LudiBoxException("Pessoa física com ID " + enderecoValidado.getPessoaFisica().getId() + " não encontrada!"));
	    validarNumeroDePf(enderecoValidado);
	    
	    enderecoValidado.getPessoaFisica().getEnderecos().add(enderecoValidado);
	}

	private void validarPessoaJuridica(Endereco enderecoValidado) throws LudiBoxException {
	    pessoaJuridicaRepository.findById(enderecoValidado.getPessoaJuridica().getId())
	        .orElseThrow(() -> new LudiBoxException("Pessoa jurídica com ID " + enderecoValidado.getPessoaJuridica().getId() + " não encontrada!"));
	    validarNumeroDePj(enderecoValidado);
	    
	    enderecoValidado.getPessoaJuridica().getEnderecos().add(enderecoValidado);
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

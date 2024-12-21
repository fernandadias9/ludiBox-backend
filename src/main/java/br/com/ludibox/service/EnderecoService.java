package br.com.ludibox.service;

import org.springframework.stereotype.Service;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Endereco;
import br.com.ludibox.model.entity.Usuario;
import br.com.ludibox.model.repository.EnderecoRepository;

@Service
public class EnderecoService {
	
	private EnderecoRepository enderecoRepository;
	
	public Endereco inserirEndereco(Endereco novoEndereco) throws LudiBoxException {
		return enderecoRepository.save(novoEndereco);
	}
	

}

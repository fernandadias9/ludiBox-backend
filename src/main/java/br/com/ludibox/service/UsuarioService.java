package br.com.ludibox.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.PessoaFisica;
import br.com.ludibox.model.entity.PessoaJuridica;
import br.com.ludibox.model.entity.Usuario;
import br.com.ludibox.model.repository.PessoaFisicaRepository;
import br.com.ludibox.model.repository.PessoaJuridicaRepository;
import br.com.ludibox.model.repository.UsuarioRepository;

@Service
public class UsuarioService {
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private PessoaFisicaRepository pessoaFisicaRepository;
	
	@Autowired
	private PessoaJuridicaRepository pessoaJuridicaRepository;
	
	public Usuario inserirUsuario(Usuario novoUsuario) throws LudiBoxException {
		return usuarioRepository.save(novoUsuario);
	}
	
	public PessoaFisica inserirPessoaFisica(PessoaFisica novo) throws LudiBoxException{
		return pessoaFisicaRepository.save(novo);
	}
	
	public PessoaJuridica inserirPessoaJuridica(PessoaJuridica novo) throws LudiBoxException{
		return pessoaJuridicaRepository.save(novo);
	}

	
	
}

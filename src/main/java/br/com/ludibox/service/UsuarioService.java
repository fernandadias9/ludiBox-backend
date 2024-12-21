package br.com.ludibox.service;

import java.util.List;

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
		verificarPfExistente(novo);
		return pessoaFisicaRepository.save(novo);
	}
	
	public PessoaJuridica inserirPessoaJuridica(PessoaJuridica novo) throws LudiBoxException{
		verificarPjExistente(novo);
		return pessoaJuridicaRepository.save(novo);
	}
	
	public void verificarPfExistente(PessoaFisica usuarioPfVerificado) throws LudiBoxException {
		List<PessoaFisica> usuariosPf = pessoaFisicaRepository.findAll();
		
		for (PessoaFisica pessoaFisica : usuariosPf) {
			if(pessoaFisica.getCpf().equals(usuarioPfVerificado.getCpf())) {
				throw new LudiBoxException("CPF já cadastrado no sistema!");
			}else if(pessoaFisica.getEmail().equals(usuarioPfVerificado.getEmail())) {
				throw new LudiBoxException("Email já cadastrado no sistema!");
			}
		}
	
	}
	
	public void verificarPjExistente(PessoaJuridica usuarioPjVerificado) throws LudiBoxException {
		List<PessoaJuridica> usuariosPj = pessoaJuridicaRepository.findAll();
		
		for (PessoaJuridica pessoaJuridica : usuariosPj) {
			if(pessoaJuridica.getCnpj().equals(usuarioPjVerificado.getCnpj())) {
				throw new LudiBoxException("CNPJ já cadastrado no sistema!");
			}else if(pessoaJuridica.getEmail().equals(usuarioPjVerificado.getEmail())) {
				throw new LudiBoxException("Email já cadastrado no sistema!");
			}
		}
	
	}
}

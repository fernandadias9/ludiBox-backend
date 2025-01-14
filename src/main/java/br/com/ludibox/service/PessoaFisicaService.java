package br.com.ludibox.service;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.PessoaFisica;
import br.com.ludibox.model.entity.PessoaJuridica;
import br.com.ludibox.model.enums.EnumPerfil;
import br.com.ludibox.model.enums.EnumStatus;
import br.com.ludibox.model.repository.PessoaFisicaRepository;
import br.com.ludibox.model.repository.PessoaJuridicaRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PessoaFisicaService  {

    @Autowired
    private PessoaFisicaRepository pessoaFisicaRepository;
    
    @Autowired
    private PessoaJuridicaRepository pessoaJuridicaRepository;
    
    @Autowired
	private AuthenticationService authService;

    public PessoaFisica salvar(PessoaFisica pessoaFisica) throws LudiBoxException {
    	verificarPessoaExistente(pessoaFisica);
        return pessoaFisicaRepository.save(pessoaFisica);
    }
    
    public PessoaFisica cadastrarAdm(PessoaFisica pessoaFisica) throws LudiBoxException{
    	PessoaFisica pfAutenticada = authService.getPessoaFisicaAutenticada();
    	if (pfAutenticada.getPerfil() == EnumPerfil.USUARIO) {
    		throw new LudiBoxException("Ação exclusiva para administradores!");
		}
    	pessoaFisica.setPerfil(EnumPerfil.ADMINISTRADOR);
    	return pessoaFisicaRepository.save(pessoaFisica);
    }
    
    public List<PessoaFisica> buscarTodosPf() throws LudiBoxException{    	
    	PessoaFisica pfAutenticada = authService.getPessoaFisicaAutenticada();
    	
    	if (pfAutenticada.getPerfil() == EnumPerfil.USUARIO) {
    		throw new LudiBoxException("Ação exclusiva para administradores!");
		}
    	
		return pessoaFisicaRepository.findAll();
    }
    
    public PessoaFisica atualizarDados(PessoaFisica pessoaFisica) throws LudiBoxException{
    	PessoaFisica pessoaAutenticada = authService.getPessoaFisicaAutenticada();   	
    	verificarDados(pessoaFisica);
    	if (pessoaAutenticada.getId() != pessoaFisica.getId()) {
			throw new LudiBoxException("Usuários só podem alterar seus próprios dados!");
		}
    	return pessoaFisicaRepository.save(pessoaFisica);
    }
    
    public void desativarPessoaFisica(PessoaFisica pessoaFisica) throws LudiBoxException{
    	PessoaFisica pessoaAutenticada = authService.getPessoaFisicaAutenticada();   	
    	if (pessoaAutenticada.getId() != pessoaFisica.getId()) {
			throw new LudiBoxException("Usuários só podem alterar seus próprios dados!");
		}
    	
    	PessoaFisica pessoa = pessoaFisicaRepository.findById(pessoaFisica.getId()).get();
    	pessoa.setSituacao(EnumStatus.INATIVO);
    	pessoaFisicaRepository.save(pessoa);
    }
    
    public void reativarPessoaFisica(PessoaFisica pessoaFisica) throws LudiBoxException{
    	PessoaFisica pfAutenticada = authService.getPessoaFisicaAutenticada();
    	
    	if (pfAutenticada.getPerfil() == EnumPerfil.USUARIO) {
    		throw new LudiBoxException("Ação exclusiva para administradores!");
		}
    	
    	
    	PessoaFisica pessoa = pessoaFisicaRepository.findById(pessoaFisica.getId()).get();
    	pessoa.setSituacao(EnumStatus.ATIVO);
    	pessoaFisicaRepository.save(pessoa);
    }
    
    public void bloquearPessoaFisica(PessoaFisica pessoaFisica) throws LudiBoxException{
    	PessoaFisica pfAutenticada = authService.getPessoaFisicaAutenticada();
    	
    	if (pfAutenticada.getPerfil() == EnumPerfil.USUARIO) {
    		throw new LudiBoxException("Ação exclusiva para administradores!");
		}
    	
    	PessoaFisica pessoa = pessoaFisicaRepository.findById(pessoaFisica.getId()).get();
    	pessoa.setSituacao(EnumStatus.BLOQUEADO);
    	pessoaFisicaRepository.save(pessoa);
    }
    
    private void verificarDados(PessoaFisica pessoaFisica) throws LudiBoxException {
    	PessoaFisica pessoaVerificada = pessoaFisicaRepository.findById(pessoaFisica.getId()).get();
		if(!pessoaVerificada.getCpf().equals(pessoaFisica.getCpf())) {
			throw new LudiBoxException("O CPF não pode ser alterado!");
		}
		if(!pessoaVerificada.getDataNascimento().equals(pessoaFisica.getDataNascimento())) {
			throw new LudiBoxException("Data de nascimento não pode ser alterada!");
		}
		
	}

	public void verificarPessoaExistente(PessoaFisica pessoaFisica) throws LudiBoxException{
    	List<PessoaFisica> pessoasPf = pessoaFisicaRepository.findAll();
    	List<PessoaJuridica> pessoasPj = pessoaJuridicaRepository.findAll();
    	
    	for (PessoaFisica pessoaPfValidada : pessoasPf) {
    		if (pessoaFisica.getCpf().equals(pessoaPfValidada.getCpf())) {
    			throw new LudiBoxException("CPF já cadastrado!");
			}else if (pessoaFisica.getEmail().equals(pessoaPfValidada.getEmail())) {
				throw new LudiBoxException("Email já cadastrado!");
			} else if (pessoaFisica.getTelefone().equals(pessoaPfValidada.getTelefone())) {
				throw new LudiBoxException("Telefone já cadastrado!");
			}
			
		}    	
    	
    	for (PessoaJuridica pessoaPjValidada : pessoasPj) {
    		if (pessoaFisica.getEmail().equals(pessoaPjValidada.getEmail())) {
				throw new LudiBoxException("Email já cadastrado!");
			} else if (pessoaFisica.getTelefone().equals(pessoaPjValidada.getTelefone())) {
				throw new LudiBoxException("Telefone já cadastrado!");
			}
			
		}
    	
    }

	

    
	
	
}

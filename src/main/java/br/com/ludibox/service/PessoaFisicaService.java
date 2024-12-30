package br.com.ludibox.service;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.PessoaFisica;
import br.com.ludibox.model.entity.PessoaJuridica;
import br.com.ludibox.model.repository.PessoaFisicaRepository;
import br.com.ludibox.model.repository.PessoaJuridicaRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PessoaFisicaService {

    @Autowired
    private PessoaFisicaRepository pessoaFisicaRepository;
    
    @Autowired
    private PessoaJuridicaRepository pessoaJuridicaRepository;

    public PessoaFisica salvar(PessoaFisica pessoaFisica) throws LudiBoxException {
    	verificarPessoaExistente(pessoaFisica);
        return pessoaFisicaRepository.save(pessoaFisica);
    }
    
    public List<PessoaFisica> buscarTodosPf(){    	
		return pessoaFisicaRepository.findAll();
    }
    
    
    public PessoaFisica atualizarDados(PessoaFisica pessoaFisica) throws LudiBoxException{
    	verificarDados(pessoaFisica);
    	return pessoaFisicaRepository.save(pessoaFisica);
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

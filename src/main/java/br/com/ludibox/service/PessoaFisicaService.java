package br.com.ludibox.service;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.PessoaFisica;
import br.com.ludibox.model.repository.PessoaFisicaRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PessoaFisicaService {

    @Autowired
    private PessoaFisicaRepository pessoaFisicaRepository;

    public PessoaFisica salvar(PessoaFisica pessoaFisica) throws LudiBoxException {
    	verificarPessoaExistente(pessoaFisica);
        return pessoaFisicaRepository.save(pessoaFisica);
    }
    
    
    public void verificarPessoaExistente(PessoaFisica pessoaFisica) throws LudiBoxException{
    	List<PessoaFisica> pessoas = pessoaFisicaRepository.findAll(); 
    	
    	for (PessoaFisica pessoaValidada : pessoas) {
    		if (pessoaFisica.getCpf().equals(pessoaValidada.getCpf())) {
    			throw new LudiBoxException("CPF já cadastrado!");
			}else if (pessoaFisica.getEmail().equals(pessoaValidada.getEmail())) {
				throw new LudiBoxException("Email já cadastrado!");
			} else if (pessoaFisica.getTelefone().equals(pessoaValidada.getTelefone())) {
				throw new LudiBoxException("Telefone já cadastrado!");
			}
			
		}    	
    }
    
    
}

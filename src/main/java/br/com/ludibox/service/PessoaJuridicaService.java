package br.com.ludibox.service;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.PessoaFisica;
import br.com.ludibox.model.entity.PessoaJuridica;
import br.com.ludibox.model.repository.PessoaJuridicaRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PessoaJuridicaService {

    @Autowired
    private PessoaJuridicaRepository pessoaJuridicaRepository;

    public PessoaJuridica salvar(PessoaJuridica pessoaJuridica) throws LudiBoxException {
    	verificarPessoaExistente(pessoaJuridica);
        return pessoaJuridicaRepository.save(pessoaJuridica);
    }
    
    public void verificarPessoaExistente(PessoaJuridica pessoaJuridica) throws LudiBoxException{
    	List<PessoaJuridica> pessoas = pessoaJuridicaRepository.findAll(); 
    	
    	for (PessoaJuridica pessoaValidada : pessoas) {
    		if (pessoaJuridica.getCnpj().equals(pessoaValidada.getCnpj())) {
    			throw new LudiBoxException("CNPJ já cadastrado!");
			}else if (pessoaJuridica.getEmail().equals(pessoaValidada.getEmail())) {
				throw new LudiBoxException("Email já cadastrado!");
			} else if (pessoaJuridica.getTelefone().equals(pessoaValidada.getTelefone())) {
				throw new LudiBoxException("Telefone já cadastrado!");
			}
			
		}    	
    }

}

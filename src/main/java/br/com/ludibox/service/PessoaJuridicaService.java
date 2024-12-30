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
public class PessoaJuridicaService {

    @Autowired
    private PessoaJuridicaRepository pessoaJuridicaRepository;
    
    @Autowired
    private PessoaFisicaRepository pessoaFisicaRepository;

    public PessoaJuridica salvar(PessoaJuridica pessoaJuridica) throws LudiBoxException {
    	verificarPessoaExistente(pessoaJuridica);
        return pessoaJuridicaRepository.save(pessoaJuridica);
    }
    
    public PessoaJuridica atualizarDados(PessoaJuridica pessoaJuridica) throws LudiBoxException{
    	verificarDados(pessoaJuridica);
    	return pessoaJuridicaRepository.save(pessoaJuridica);
    }
    
    public List<PessoaJuridica> buscarTodosPj(){
    	return pessoaJuridicaRepository.findAll();
    }
    
    private void verificarDados(PessoaJuridica pessoaJuridica) throws LudiBoxException {
    	PessoaJuridica pessoaVerificada = pessoaJuridicaRepository.findById(pessoaJuridica.getId()).get();
		if(!pessoaVerificada.getCnpj().equals(pessoaJuridica.getCnpj())) {
			throw new LudiBoxException("O CNPJ não pode ser alterado!");
		}		
	}

    public void verificarPessoaExistente(PessoaJuridica pessoaJuridica) throws LudiBoxException{
    	List<PessoaJuridica> pessoasPj = pessoaJuridicaRepository.findAll(); 
    	List<PessoaFisica> pessoasPf = pessoaFisicaRepository.findAll();
    	
    	for (PessoaJuridica pessoaValidada : pessoasPj) {
    		if (pessoaJuridica.getCnpj().equals(pessoaValidada.getCnpj())) {
    			throw new LudiBoxException("CNPJ já cadastrado!");
			}else if (pessoaJuridica.getEmail().equals(pessoaValidada.getEmail())) {
				throw new LudiBoxException("Email já cadastrado!");
			} else if (pessoaJuridica.getTelefone().equals(pessoaValidada.getTelefone())) {
				throw new LudiBoxException("Telefone já cadastrado!");
			}
			
		}    
    	
    	for (PessoaFisica pessoaPfValidada : pessoasPf) {
    		if (pessoaJuridica.getEmail().equals(pessoaPfValidada.getEmail())) {
				throw new LudiBoxException("Email já cadastrado!");
			} else if (pessoaJuridica.getTelefone().equals(pessoaPfValidada.getTelefone())) {
				throw new LudiBoxException("Telefone já cadastrado!");
			}
			
		}  
    }

}

package br.com.ludibox.service;

import br.com.ludibox.model.entity.PessoaJuridica;
import br.com.ludibox.model.repository.PessoaJuridicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PessoaJuridicaService {

    @Autowired
    private PessoaJuridicaRepository repository;

    public PessoaJuridica salvar(PessoaJuridica pessoaJuridica) {
        return repository.save(pessoaJuridica);
    }
}

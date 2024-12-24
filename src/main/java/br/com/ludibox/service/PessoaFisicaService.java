package br.com.ludibox.service;

import br.com.ludibox.model.entity.PessoaFisica;
import br.com.ludibox.model.repository.PessoaFisicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PessoaFisicaService {

    @Autowired
    private PessoaFisicaRepository repository;

    public PessoaFisica salvar(PessoaFisica pessoaFisica) {
        return repository.save(pessoaFisica);
    }
}

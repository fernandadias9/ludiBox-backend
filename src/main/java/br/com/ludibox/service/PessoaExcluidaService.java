package br.com.ludibox.service;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.entity.PessoaExcluida;
import br.com.ludibox.model.enums.EnumStatus;
import br.com.ludibox.model.repository.PessoaExcluidaRepository;
import br.com.ludibox.model.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class PessoaExcluidaService {

    @Autowired
    PessoaExcluidaRepository pessoaExcluidaRepository;

    @Autowired
    PessoaRepository pessoaRepository;

    @Autowired
    AuthenticationService authenticationService;

    public void excluirPessoa(int id) throws LudiBoxException {
        Pessoa pessoaAutenticada = authenticationService.getPessoaAutenticada();
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new LudiBoxException("ID: ", "Pessoa não encontrada!", HttpStatus.BAD_REQUEST));

        if (!pessoaAutenticada.getId().equals(pessoa.getId())) {
            throw new LudiBoxException("Erro: ", "Usuários só podem excluir seus próprios dados!", HttpStatus.UNAUTHORIZED);
        }

        PessoaExcluida pessoaExcluida = new PessoaExcluida();
        pessoaExcluida.setCpfOuCnpj(pessoa.getValorDocumento());
        pessoaExcluida.setEmail(pessoa.getEmail());
        pessoaExcluida.setDataExclusao(LocalDateTime.now());

        pessoaExcluidaRepository.save(pessoaExcluida);

        pessoa.setSituacao(EnumStatus.EXCLUIDO);
        pessoaRepository.save(pessoa);
    }

}

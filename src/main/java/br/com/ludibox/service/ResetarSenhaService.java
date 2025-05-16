package br.com.ludibox.service;

import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ResetarSenhaService {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder codificadorDeSenha;

    public void resetarSenha(String email) {
        Pessoa pessoa = pessoaRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("E-mail não encontrado."));

        String novaSenha = gerarSenhaAleatoria();
        pessoa.setSenha(codificadorDeSenha.encode(novaSenha));
        pessoaRepository.save(pessoa);

        emailService.enviarNovaSenha(email, novaSenha);
    }

    private String gerarSenhaAleatoria() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}

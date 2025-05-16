package br.com.ludibox.service;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.dto.SenhasDTO;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.IllegalFormatCodePointException;
import java.util.UUID;

@Service
public class ResetarSenhaService {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder codificadorDeSenha;

    @Autowired
    private AuthenticationService authService;


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

    public void alterarSenha(SenhasDTO senhasDTO) throws LudiBoxException {
        if (senhasDTO == null ||
                senhasDTO.senhaAtual == null || senhasDTO.senhaAtual.length() < 5 ||
                senhasDTO.novaSenha == null || senhasDTO.novaSenha.length() < 5 ||
                senhasDTO.confirmarSenha == null || senhasDTO.confirmarSenha.length() < 5) {
            throw new LudiBoxException("Erro: ", "Valores não inseridos ou senhas muito curtas!", HttpStatus.BAD_REQUEST);
        }

        Pessoa pessoaAutenticada = authService.getPessoaAutenticada();

        if (senhasDTO.senhaAtual.equals(senhasDTO.novaSenha)) {
            throw new LudiBoxException("Erro:", "Senha atual inválida", HttpStatus.BAD_REQUEST);
        }

        if (!senhasDTO.novaSenha.equals(senhasDTO.confirmarSenha)) {
            throw new LudiBoxException("Erro: ", "Nova senha e confirmação não coincidem", HttpStatus.BAD_REQUEST);
        }

        if (!codificadorDeSenha.matches(senhasDTO.senhaAtual, pessoaAutenticada.getSenha())) {
            throw new LudiBoxException("Erro:", "Senha atual inválida", HttpStatus.BAD_REQUEST);
        }

        String senhaCodificada = codificadorDeSenha.encode(senhasDTO.novaSenha);
        pessoaAutenticada.setSenha(senhaCodificada);
        pessoaRepository.save(pessoaAutenticada);
    }

}

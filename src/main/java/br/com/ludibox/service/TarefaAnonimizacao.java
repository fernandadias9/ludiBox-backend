package br.com.ludibox.service;

import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TarefaAnonimizacao {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private PessoaService pessoaService;

    // Roda todo dia às 3h da manhã
    @Scheduled(cron = "0 0 3 * * *")
    public void anonimizarUsuariosInativos() {
        LocalDateTime dataLimite = LocalDateTime.now().minusYears(2);
        List<Pessoa> pessoasParaAnonimizar = pessoaRepository
                .findAllByDataDesativacaoBeforeAndEmailNotLike(dataLimite, "anonimizado_%");

        pessoasParaAnonimizar.forEach(p -> {
            pessoaService.anonimizarDadosPessoa(p.getId());
        });
    }
}

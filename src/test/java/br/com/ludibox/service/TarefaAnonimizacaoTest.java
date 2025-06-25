package br.com.ludibox.service;

import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.repository.PessoaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class TarefaAnonimizacaoTest {

    @InjectMocks
    private TarefaAnonimizacao tarefaAnonimizacao;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private PessoaService pessoaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void anonimizarUsuariosInativos_DeveChamarAnonimizacaoParaUsuariosValidos() {
        // Arrange
        LocalDateTime dataLimiteEsperada = LocalDateTime.now().minusYears(1);

        Pessoa p1 = new Pessoa();
        p1.setId(1);

        Pessoa p2 = new Pessoa();
        p2.setId(2);

        List<Pessoa> pessoasParaAnonimizar = Arrays.asList(p1, p2);

        when(pessoaRepository.findAllByDataDesativacaoBeforeAndEmailNotLike(any(LocalDateTime.class), eq("anonimizado_%")))
                .thenReturn(pessoasParaAnonimizar);

        // Act
        tarefaAnonimizacao.anonimizarUsuariosInativos();

        // Assert
        verify(pessoaRepository).findAllByDataDesativacaoBeforeAndEmailNotLike(any(LocalDateTime.class), eq("anonimizado_%"));
        verify(pessoaService).anonimizarDadosPessoa(1);
        verify(pessoaService).anonimizarDadosPessoa(2);
        verifyNoMoreInteractions(pessoaService);
    }

    @Test
    void anonimizarUsuariosInativos_NaoDeveChamarAnonimizacaoQuandoListaVazia() {
        // Arrange
        when(pessoaRepository.findAllByDataDesativacaoBeforeAndEmailNotLike(any(LocalDateTime.class), eq("anonimizado_%")))
                .thenReturn(Collections.emptyList());

        // Act
        tarefaAnonimizacao.anonimizarUsuariosInativos();

        // Assert
        verify(pessoaRepository).findAllByDataDesativacaoBeforeAndEmailNotLike(any(LocalDateTime.class), eq("anonimizado_%"));
        verifyNoInteractions(pessoaService);
    }

    @Test
    void verificarAnotacaoScheduled() throws NoSuchMethodException {
        Scheduled scheduled = TarefaAnonimizacao.class
                .getMethod("anonimizarUsuariosInativos")
                .getAnnotation(Scheduled.class);

        assert scheduled != null;
        assert scheduled.cron().equals("0 0 3 1 * *");
    }
}

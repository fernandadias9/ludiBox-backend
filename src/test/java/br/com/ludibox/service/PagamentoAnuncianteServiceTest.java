package br.com.ludibox.service;

import br.com.ludibox.model.entity.PagamentosAnunciante;
import br.com.ludibox.model.repository.PagamentosAnuncianteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PagamentosAnuncianteServiceTest {

    @InjectMocks
    private PagamentosAnuncianteService pagamentosAnuncianteService;

    @Mock
    private PagamentosAnuncianteRepository repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSalvarPagamentoSucesso() {
        // Arrange
        PagamentosAnunciante novoPagamento = new PagamentosAnunciante();
        novoPagamento.setDataLimiteLiberacao(LocalDate.of(2025, 12, 31));
        novoPagamento.setNomeAnunciante("Anunciante Teste");
        novoPagamento.setTipoChavePix("CPF");
        novoPagamento.setValorChavePix("12345678900");
        novoPagamento.setValor(BigDecimal.valueOf(150.50)); // CORREÇÃO

        PagamentosAnunciante pagamentoSalvo = new PagamentosAnunciante();
        pagamentoSalvo.setDataLimiteLiberacao(novoPagamento.getDataLimiteLiberacao());
        pagamentoSalvo.setNomeAnunciante(novoPagamento.getNomeAnunciante());
        pagamentoSalvo.setTipoChavePix(novoPagamento.getTipoChavePix());
        pagamentoSalvo.setValorChavePix(novoPagamento.getValorChavePix());
        pagamentoSalvo.setValor(novoPagamento.getValor());

        when(repository.save(any(PagamentosAnunciante.class))).thenReturn(pagamentoSalvo);

        // Act
        PagamentosAnunciante resultado = pagamentosAnuncianteService.salvar(novoPagamento);

        // Assert
        assertNotNull(resultado);
        assertEquals(novoPagamento.getDataLimiteLiberacao(), resultado.getDataLimiteLiberacao());
        assertEquals(novoPagamento.getNomeAnunciante(), resultado.getNomeAnunciante());
        assertEquals(novoPagamento.getTipoChavePix(), resultado.getTipoChavePix());
        assertEquals(novoPagamento.getValorChavePix(), resultado.getValorChavePix());
        assertEquals(novoPagamento.getValor(), resultado.getValor());

        verify(repository, times(1)).save(any(PagamentosAnunciante.class));
    }
}
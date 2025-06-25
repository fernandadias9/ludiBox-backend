package br.com.ludibox.service;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.model.entity.*;
import br.com.ludibox.model.enums.StatusLocacao;
import br.com.ludibox.model.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocacaoServiceTest {

    @InjectMocks
    private LocacaoService locacaoService;

    @Mock
    private LocacaoRepository locacaoRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private ProdutoLocacaoRepository produtoLocacaoRepository;
    @Mock
    private CancelamentoRepository cancelamentoRepository;
    @Mock
    private EnderecoRepository enderecoRepository;
    @Mock
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAbrirNovaLocacao_ComSucesso() {
        Produto produto = new Produto();
        produto.setId(1);
        produto.setDatasIndisponiveis(new ArrayList<>());

        ProdutoLocacao produtoLocacao = new ProdutoLocacao();
        produtoLocacao.setProduto(produto);
        produtoLocacao.setDataInicio(LocalDate.now());
        produtoLocacao.setDataFim(LocalDate.now().plusDays(2));
        produtoLocacao.setValorDiario(100.0);

        Locacao locacao = new Locacao();
        locacao.setLocador(new Pessoa());
        locacao.getLocador().setId(1);
        locacao.setProdutos(new ArrayList<>(List.of(produtoLocacao)));

        when(locacaoRepository.findByUsuarioIdAndStatusPendente(1)).thenReturn(Optional.empty());
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(locacaoRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Locacao result = locacaoService.abrirNovaLocacao(locacao);

        assertNotNull(result);
        assertEquals(StatusLocacao.PENDENTE, result.getStatus());
        assertEquals(300.0, result.getValorTotal());
    }

    @Test
    void testIncluirProdutoNaLocacao_ComSucesso() {
        Produto produto = new Produto();
        produto.setId(1);
        produto.setDatasIndisponiveis(new ArrayList<>());

        ProdutoLocacao novoProduto = new ProdutoLocacao();
        novoProduto.setProduto(produto);
        novoProduto.setDataInicio(LocalDate.now());
        novoProduto.setDataFim(LocalDate.now().plusDays(2));
        novoProduto.setValorDiario(50.0);

        Locacao locacao = new Locacao();
        locacao.setId(1);
        locacao.setProdutos(new ArrayList<>());

        when(locacaoRepository.findById(1)).thenReturn(Optional.of(locacao));
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(produtoLocacaoRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(locacaoRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Locacao result = locacaoService.incluirProdutoNaLocacao(1, novoProduto);

        assertNotNull(result);
        assertEquals(150.0, result.getValorTotal());
        assertEquals(1, result.getProdutos().size());
    }

    @Test
    void testRetirarProdutoDaLocacao_ComSucesso() {
        Produto produto = new Produto();
        produto.setDatasIndisponiveis(new ArrayList<>(List.of(LocalDate.now(), LocalDate.now().plusDays(1))));

        ProdutoLocacao pl = new ProdutoLocacao();
        pl.setId(1);
        pl.setProduto(produto);
        pl.setDataInicio(LocalDate.now());
        pl.setDataFim(LocalDate.now().plusDays(1));
        pl.setValorDiario(100.0);

        Locacao locacao = new Locacao();
        locacao.setId(1);
        locacao.setProdutos(new ArrayList<>(List.of(pl)));

        when(locacaoRepository.findById(1)).thenReturn(Optional.of(locacao));
        when(locacaoRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Locacao result = locacaoService.retirarProdutoDaLocacao(1, 1);

        assertNotNull(result);
        assertTrue(result.getProdutos().isEmpty());
        assertEquals(0.0, result.getValorTotal());
    }

    @Test
    void testCalcularValorTotal() {
        ProdutoLocacao pl1 = new ProdutoLocacao();
        pl1.setDataInicio(LocalDate.now());
        pl1.setDataFim(LocalDate.now().plusDays(2));
        pl1.setValorDiario(100.0);

        ProdutoLocacao pl2 = new ProdutoLocacao();
        pl2.setDataInicio(LocalDate.now());
        pl2.setDataFim(LocalDate.now().plusDays(1));
        pl2.setValorDiario(50.0);

        Locacao locacao = new Locacao();
        locacao.setProdutos(List.of(pl1, pl2));

        double total = locacaoService.calcularValorTotal(locacao);
        assertEquals(300.0 + 100.0, total);
    }

    @Test
    void testBuscarLocacaoPendentePorUsuarioId() {
        Optional<Locacao> loc = Optional.of(new Locacao());
        when(locacaoRepository.findByUsuarioIdAndStatusPendente(1)).thenReturn(loc);

        Optional<Locacao> result = locacaoService.buscarLocacaoPendentePorUsuarioId(1);

        assertTrue(result.isPresent());
    }

    @Test
    void testBuscarPorId_ComSucesso() {
        Locacao locacao = new Locacao();
        locacao.setId(1);
        when(locacaoRepository.findById(1)).thenReturn(Optional.of(locacao));

        Locacao result = locacaoService.buscarPorId(1);
        assertEquals(1, result.getId());
    }

    @Test
    void testAtualizarStatus() {
        Locacao locacao = new Locacao();
        locacao.setId(1);
        locacao.setStatus(StatusLocacao.PENDENTE);

        when(locacaoRepository.findById(1)).thenReturn(Optional.of(locacao));
        when(locacaoRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Locacao result = locacaoService.atualizarStatus(1, "CANCELADO");

        assertEquals(StatusLocacao.CANCELADO, result.getStatus());
    }
}

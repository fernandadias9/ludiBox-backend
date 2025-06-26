package br.com.ludibox.service;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.model.entity.*;
import br.com.ludibox.model.enums.StatusLocacao;
import br.com.ludibox.model.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private Pessoa pessoa;
    private Produto produto;
    private Locacao locacao;
    private ProdutoLocacao produtoLocacao;
    private Endereco endereco;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup common test data
        pessoa = new Pessoa();
        pessoa.setId(1);

        produto = new Produto();
        produto.setId(1);
        produto.setDatasIndisponiveis(new ArrayList<>());

        produtoLocacao = new ProdutoLocacao();
        produtoLocacao.setId(1);
        produtoLocacao.setProduto(produto);
        produtoLocacao.setDataInicio(LocalDate.now());
        produtoLocacao.setDataFim(LocalDate.now().plusDays(2));
        produtoLocacao.setValorDiario(100.0);

        locacao = new Locacao();
        locacao.setId(1);
        locacao.setLocador(pessoa);
        locacao.setProdutos(new ArrayList<>(List.of(produtoLocacao)));
        locacao.setStatus(StatusLocacao.PENDENTE);

        endereco = new Endereco();
        endereco.setId(1);
        endereco.setPessoa(pessoa);
    }


    @Test
    void testAbrirNovaLocacao_ComLocacaoExistente_DeveLancarExcecao() {
        when(locacaoRepository.findByUsuarioIdAndStatusPendente(1)).thenReturn(Optional.of(new Locacao()));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> locacaoService.abrirNovaLocacao(locacao));

        assertEquals("Erro ao iniciar locação.", exception.getMessage());
    }

    @Test
    void testAbrirNovaLocacao_SemProdutos_DeveLancarExcecao() {
        locacao.setProdutos(new ArrayList<>());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> locacaoService.abrirNovaLocacao(locacao));

        assertEquals("A locação inicial deve conter exatamente um produto.", exception.getMessage());
    }

    @Test
    void testAbrirNovaLocacao_ComMaisDeUmProduto_DeveLancarExcecao() {
        ProdutoLocacao segundoProduto = new ProdutoLocacao();
        locacao.getProdutos().add(segundoProduto);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> locacaoService.abrirNovaLocacao(locacao));

        assertEquals("A locação inicial deve conter exatamente um produto.", exception.getMessage());
    }

    @Test
    void testAbrirNovaLocacao_ProdutoNaoEncontrado_DeveLancarExcecao() {
        when(locacaoRepository.findByUsuarioIdAndStatusPendente(1)).thenReturn(Optional.empty());
        when(produtoRepository.findById(1)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> locacaoService.abrirNovaLocacao(locacao));

        assertEquals("Produto não encontrado", exception.getMessage());
    }

    // ========== TESTES PARA incluirProdutoNaLocacao ==========

    @Test
    void testIncluirProdutoNaLocacao_ComSucesso() {
        ProdutoLocacao novoProduto = new ProdutoLocacao();
        novoProduto.setProduto(produto);
        novoProduto.setDataInicio(LocalDate.now());
        novoProduto.setDataFim(LocalDate.now().plusDays(2));
        novoProduto.setValorDiario(50.0);

        locacao.setProdutos(new ArrayList<>());

        when(locacaoRepository.findById(1)).thenReturn(Optional.of(locacao));
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(produtoLocacaoRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(locacaoRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Locacao result = locacaoService.incluirProdutoNaLocacao(1, novoProduto);

        assertNotNull(result);
        assertEquals(150.0, result.getValorTotal());
        assertEquals(1, result.getProdutos().size());
        verify(produtoRepository).save(produto);
    }

    @Test
    void testIncluirProdutoNaLocacao_LocacaoNaoEncontrada_DeveLancarExcecao() {
        when(locacaoRepository.findById(1)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> locacaoService.incluirProdutoNaLocacao(1, produtoLocacao));

        assertEquals("Locação não encontrada", exception.getMessage());
    }

    @Test
    void testIncluirProdutoNaLocacao_ProdutoNaoEncontrado_DeveLancarExcecao() {
        when(locacaoRepository.findById(1)).thenReturn(Optional.of(locacao));
        when(produtoRepository.findById(1)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> locacaoService.incluirProdutoNaLocacao(1, produtoLocacao));

        assertEquals("Produto não encontrado", exception.getMessage());
    }

    // ========== TESTES PARA retirarProdutoDaLocacao ==========

    @Test
    void testRetirarProdutoDaLocacao_ComSucesso() {
        produto.setDatasIndisponiveis(new ArrayList<>(List.of(LocalDate.now(), LocalDate.now().plusDays(1))));

        when(locacaoRepository.findById(1)).thenReturn(Optional.of(locacao));
        when(locacaoRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Locacao result = locacaoService.retirarProdutoDaLocacao(1, 1);

        assertNotNull(result);
        assertTrue(result.getProdutos().isEmpty());
        assertEquals(0.0, result.getValorTotal());
        verify(produtoLocacaoRepository).delete(produtoLocacao);
        verify(produtoRepository).save(produto);
    }

    @Test
    void testRetirarProdutoDaLocacao_LocacaoNaoEncontrada_DeveLancarExcecao() {
        when(locacaoRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> locacaoService.retirarProdutoDaLocacao(1, 1));

        assertEquals("Locação não encontrada", exception.getMessage());
    }

    @Test
    void testRetirarProdutoDaLocacao_ProdutoNaoEncontrado_DeveLancarExcecao() {
        when(locacaoRepository.findById(1)).thenReturn(Optional.of(locacao));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> locacaoService.retirarProdutoDaLocacao(1, 999));

        assertEquals("Produto na locação não encontrado", exception.getMessage());
    }

    // ========== TESTES PARA calcularValorTotal ==========

    @Test
    void testCalcularValorTotal_ComMultiplosProdutos() {
        ProdutoLocacao pl1 = new ProdutoLocacao();
        pl1.setDataInicio(LocalDate.now());
        pl1.setDataFim(LocalDate.now().plusDays(2));
        pl1.setValorDiario(100.0);

        ProdutoLocacao pl2 = new ProdutoLocacao();
        pl2.setDataInicio(LocalDate.now());
        pl2.setDataFim(LocalDate.now().plusDays(1));
        pl2.setValorDiario(50.0);

        locacao.setProdutos(List.of(pl1, pl2));

        double total = locacaoService.calcularValorTotal(locacao);

        assertEquals(400.0, total); // (3 * 100) + (2 * 50)
    }

    @Test
    void testCalcularValorTotal_SemProdutos() {
        locacao.setProdutos(new ArrayList<>());

        double total = locacaoService.calcularValorTotal(locacao);

        assertEquals(0.0, total);
    }


    @Test
    void testDeletarLocacao_ComSucesso() {
        produto.setDatasIndisponiveis(new ArrayList<>(List.of(LocalDate.now(), LocalDate.now().plusDays(1))));

        when(locacaoRepository.findById(1)).thenReturn(Optional.of(locacao));

        locacaoService.deletarLocacao(1);

        verify(produtoRepository).save(produto);
        verify(produtoLocacaoRepository).delete(produtoLocacao);
        verify(locacaoRepository).delete(locacao);
    }

    @Test
    void testDeletarLocacao_LocacaoNaoEncontrada_DeveLancarExcecao() {
        when(locacaoRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> locacaoService.deletarLocacao(1));

        assertEquals("Locação não encontrada", exception.getMessage());
    }

    @Test
    void testCancelarLocacao_LocacaoNaoEncontrada_DeveLancarExcecao() {
        when(locacaoRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> locacaoService.cancelarLocacao(1, "Motivo"));

        assertEquals("Locação não encontrada", exception.getMessage());
    }

    @Test
    void testEscolherEnderecoEntrega_ComSucesso() {
        when(locacaoRepository.findById(1)).thenReturn(Optional.of(locacao));
        when(enderecoRepository.findById(1)).thenReturn(Optional.of(endereco));
        when(locacaoRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        locacaoService.escolherEnderecoEntrega(1, 1, 1);

        assertEquals(endereco, locacao.getEnderecoEntrega());
        verify(locacaoRepository).save(locacao);
    }

    @Test
    void testEscolherEnderecoEntrega_LocacaoNaoEncontrada_DeveLancarExcecao() {
        when(locacaoRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> locacaoService.escolherEnderecoEntrega(1, 1, 1));

        assertEquals("Locação não encontrada", exception.getMessage());
    }

    @Test
    void testEscolherEnderecoEntrega_EnderecoNaoEncontrado_DeveLancarExcecao() {
        when(locacaoRepository.findById(1)).thenReturn(Optional.of(locacao));
        when(enderecoRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> locacaoService.escolherEnderecoEntrega(1, 1, 1));

        assertEquals("Endereço não encontrado", exception.getMessage());
    }

    @Test
    void testEscolherEnderecoEntrega_EnderecoNaoPertenceAoLocador_DeveLancarExcecao() {
        Pessoa outraPessoa = new Pessoa();
        outraPessoa.setId(2);
        endereco.setPessoa(outraPessoa);

        when(locacaoRepository.findById(1)).thenReturn(Optional.of(locacao));
        when(enderecoRepository.findById(1)).thenReturn(Optional.of(endereco));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> locacaoService.escolherEnderecoEntrega(1, 1, 1));

        assertEquals("Endereço não pertence ao locador", exception.getMessage());
    }

    @Test
    void testBuscarLocacaoPendentePorUsuarioId_ComLocacaoExistente() {
        Optional<Locacao> locacaoExistente = Optional.of(locacao);
        when(locacaoRepository.findByUsuarioIdAndStatusPendente(1)).thenReturn(locacaoExistente);

        Optional<Locacao> result = locacaoService.buscarLocacaoPendentePorUsuarioId(1);

        assertTrue(result.isPresent());
        assertEquals(locacao, result.get());
    }

    @Test
    void testBuscarLocacaoPendentePorUsuarioId_SemLocacaoExistente() {
        when(locacaoRepository.findByUsuarioIdAndStatusPendente(1)).thenReturn(Optional.empty());

        Optional<Locacao> result = locacaoService.buscarLocacaoPendentePorUsuarioId(1);

        assertFalse(result.isPresent());
    }

    @Test
    void testBuscarPorId_ComSucesso() {
        when(locacaoRepository.findById(1)).thenReturn(Optional.of(locacao));

        Locacao result = locacaoService.buscarPorId(1);

        assertEquals(locacao, result);
    }

    @Test
    void testBuscarPorId_LocacaoNaoEncontrada_DeveLancarExcecao() {
        when(locacaoRepository.findById(1)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> locacaoService.buscarPorId(1));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Locação não encontrada", exception.getReason());
    }

    @Test
    void testFinalizarLocacao_ComSucesso() {
        when(locacaoRepository.findById(1)).thenReturn(Optional.of(locacao));
        when(enderecoRepository.findById(1)).thenReturn(Optional.of(endereco));
        when(locacaoRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        locacaoService.finalizarLocacao(1, 1, 1);

        assertEquals(StatusLocacao.PAGO, locacao.getStatus());
        assertNotNull(locacao.getDataHoraPagamento());
        assertEquals(endereco, locacao.getEnderecoEntrega());
    }

    @Test
    void testFinalizarLocacao_LocacaoNaoEncontrada_DeveLancarExcecao() {
        when(locacaoRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> locacaoService.finalizarLocacao(1, 1, 1));

        assertEquals("Locação não encontrada", exception.getMessage());
    }

    @Test
    void testFinalizarLocacao_LocacaoNaoPertenceAoUsuario_DeveLancarExcecao() {
        Pessoa outraPessoa = new Pessoa();
        outraPessoa.setId(2);
        locacao.setLocador(outraPessoa);

        when(locacaoRepository.findById(1)).thenReturn(Optional.of(locacao));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> locacaoService.finalizarLocacao(1, 1, 1));

        assertEquals("Locação não pertence ao usuário", exception.getMessage());
    }

    @Test
    void testObterLocacoesRecebidas() {
        List<ProdutoLocacao> locacoesEsperadas = List.of(produtoLocacao);
        when(produtoLocacaoRepository.findLocacoesRecebidas(1)).thenReturn(locacoesEsperadas);

        List<ProdutoLocacao> result = locacaoService.obterLocacoesRecebidas(1);

        assertEquals(locacoesEsperadas, result);
    }


    @Test
    void testObterLocacoesEfetuadas() {
        List<ProdutoLocacao> locacoesEsperadas = List.of(produtoLocacao);
        when(authenticationService.getPessoaAutenticada()).thenReturn(pessoa);
        when(produtoLocacaoRepository.findByLocador(pessoa)).thenReturn(locacoesEsperadas);

        List<ProdutoLocacao> result = locacaoService.obterLocacoesEfetuadas();

        assertEquals(locacoesEsperadas, result);
    }

    @Test
    void testAtualizarStatus_ComSucesso() {
        when(locacaoRepository.findById(1)).thenReturn(Optional.of(locacao));
        when(locacaoRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Locacao result = locacaoService.atualizarStatus(1, "CANCELADO");

        assertEquals(StatusLocacao.CANCELADO, result.getStatus());
    }

    @Test
    void testAtualizarStatus_LocacaoNaoEncontrada_DeveLancarExcecao() {
        when(locacaoRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> locacaoService.atualizarStatus(1, "CANCELADO"));

        assertEquals("Locação não encontrada", exception.getMessage());
    }

    @Test
    void testAtualizarStatus_StatusInvalido_DeveLancarExcecao() {
        when(locacaoRepository.findById(1)).thenReturn(Optional.of(locacao));

        assertThrows(IllegalArgumentException.class,
                () -> locacaoService.atualizarStatus(1, "STATUS_INVALIDO"));
    }

    @Test
    void testObterTodasAsLocacoes() {
        List<Locacao> locacoesEsperadas = List.of(locacao);
        when(locacaoRepository.findAll()).thenReturn(locacoesEsperadas);

        List<Locacao> result = locacaoService.obterTodasAsLocacoes();

        assertEquals(locacoesEsperadas, result);
    }

    @Test
    void testFiltrarLocacoes_SemFiltros() {
        List<Locacao> todasLocacoes = List.of(locacao);
        when(locacaoRepository.findAll()).thenReturn(todasLocacoes);

        List<Locacao> result = locacaoService.filtrarLocacoes(null, null, null, null);

        assertEquals(todasLocacoes, result);
    }

    @Test
    void testFiltrarLocacoes_ComFiltroDataInicio() {
        locacao.setDataHoraEfetuada(LocalDateTime.now());
        locacao.setValorTotal(500.0);

        List<Locacao> todasLocacoes = List.of(locacao);
        when(locacaoRepository.findAll()).thenReturn(todasLocacoes);

        String dataInicio = LocalDate.now().minusDays(1).toString();
        List<Locacao> result = locacaoService.filtrarLocacoes(dataInicio, null, null, null);

        assertEquals(1, result.size());
        assertEquals(locacao, result.get(0));
    }

    @Test
    void testFiltrarLocacoes_ComFiltroDataFim() {
        locacao.setDataHoraEfetuada(LocalDateTime.now());
        locacao.setValorTotal(500.0);

        List<Locacao> todasLocacoes = List.of(locacao);
        when(locacaoRepository.findAll()).thenReturn(todasLocacoes);

        String dataFim = LocalDate.now().plusDays(1).toString();
        List<Locacao> result = locacaoService.filtrarLocacoes(null, dataFim, null, null);

        assertEquals(1, result.size());
        assertEquals(locacao, result.get(0));
    }

    @Test
    void testFiltrarLocacoes_ComFiltroValorMinimo() {
        locacao.setDataHoraEfetuada(LocalDateTime.now());
        locacao.setValorTotal(500.0);

        List<Locacao> todasLocacoes = List.of(locacao);
        when(locacaoRepository.findAll()).thenReturn(todasLocacoes);

        List<Locacao> result = locacaoService.filtrarLocacoes(null, null, 400.0, null);

        assertEquals(1, result.size());
        assertEquals(locacao, result.get(0));
    }

    @Test
    void testFiltrarLocacoes_ComFiltroValorMaximo() {
        locacao.setDataHoraEfetuada(LocalDateTime.now());
        locacao.setValorTotal(500.0);

        List<Locacao> todasLocacoes = List.of(locacao);
        when(locacaoRepository.findAll()).thenReturn(todasLocacoes);

        List<Locacao> result = locacaoService.filtrarLocacoes(null, null, null, 600.0);

        assertEquals(1, result.size());
        assertEquals(locacao, result.get(0));
    }

    @Test
    void testFiltrarLocacoes_ComTodosFiltros_LocacaoValida() {
        locacao.setDataHoraEfetuada(LocalDateTime.now());
        locacao.setValorTotal(500.0);

        List<Locacao> todasLocacoes = List.of(locacao);
        when(locacaoRepository.findAll()).thenReturn(todasLocacoes);

        String dataInicio = LocalDate.now().minusDays(1).toString();
        String dataFim = LocalDate.now().plusDays(1).toString();

        List<Locacao> result = locacaoService.filtrarLocacoes(dataInicio, dataFim, 400.0, 600.0);

        assertEquals(1, result.size());
        assertEquals(locacao, result.get(0));
    }

    @Test
    void testFiltrarLocacoes_ComTodosFiltros_LocacaoInvalida() {
        locacao.setDataHoraEfetuada(LocalDateTime.now());
        locacao.setValorTotal(500.0);

        List<Locacao> todasLocacoes = List.of(locacao);
        when(locacaoRepository.findAll()).thenReturn(todasLocacoes);

        String dataInicio = LocalDate.now().plusDays(1).toString(); // Data futura
        String dataFim = LocalDate.now().plusDays(2).toString();

        List<Locacao> result = locacaoService.filtrarLocacoes(dataInicio, dataFim, 400.0, 600.0);

        assertTrue(result.isEmpty());
    }

    @Test
    void testFiltrarLocacoes_LocacaoSemDataHora() {
        locacao.setDataHoraEfetuada(null);
        locacao.setValorTotal(500.0);

        List<Locacao> todasLocacoes = List.of(locacao);
        when(locacaoRepository.findAll()).thenReturn(todasLocacoes);

        String dataInicio = LocalDate.now().toString();
        List<Locacao> result = locacaoService.filtrarLocacoes(dataInicio, null, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void testFiltrarLocacoes_LocacaoSemValorTotal() {
        locacao.setDataHoraEfetuada(LocalDateTime.now());
        locacao.setValorTotal(null);

        List<Locacao> todasLocacoes = List.of(locacao);
        when(locacaoRepository.findAll()).thenReturn(todasLocacoes);

        List<Locacao> result = locacaoService.filtrarLocacoes(null, null, 100.0, null);

        assertTrue(result.isEmpty());
    }
}
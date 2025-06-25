//package br.com.ludibox.service;
//
//import br.com.ludibox.auth.AuthenticationService;
//import br.com.ludibox.exception.LudiBoxException;
//import br.com.ludibox.model.entity.*;
//import br.com.ludibox.model.enums.StatusLocacao;
//import br.com.ludibox.model.repository.*;
//import br.com.ludibox.service.IA.ValidadorConteudoService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.time.LocalDate;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class AvaliacaoServiceTest {
//
//    @InjectMocks
//    private ProdutoService produtoService;
//
//    @InjectMocks
//    private AvaliacaoService avaliacaoService;
//
//    @Mock
//    private ProdutoRepository produtoRepository;
//
//    @Mock
//    private AuthenticationService authService;
//
//    @Mock
//    private ImagemService imagemService;
//
//    @Mock
//    private ValidadorConteudoService validadorConteudoService;
//
//    @Mock
//    private ProdutoLocacaoRepository produtoLocacaoRepository;
//
//    @Mock
//    private AvaliacaoRepository avaliacaoRepository;
//
//    @Mock
//    private PessoaRepository pessoaRepository;
//
//    @Mock
//    private MultipartFile imagem;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    // ... (outros testes do ProdutoService e AuthService)
//
//    @Test
//    void testSalvarAvaliacaoComSucesso() {
//        ProdutoLocacao pl = new ProdutoLocacao();
//        Pessoa locador = new Pessoa();
//        locador.setId(1);
//        Locacao locacao = new Locacao();
//        locacao.setStatus(StatusLocacao.PAGO);
//        locacao.setLocador(locador);
//        pl.setLocacao(locacao);
//        pl.setDataFim(LocalDate.now().minusDays(1));
//        pl.setAvaliado(false);
//
//        when(produtoLocacaoRepository.findById(1)).thenReturn(Optional.of(pl));
//
//        Pessoa avaliador = new Pessoa();
//        avaliador.setId(1);
//        when(pessoaRepository.findById(1)).thenReturn(Optional.of(avaliador));
//
//        when(avaliacaoRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
//
//        Avaliacao resultado = avaliacaoService.salvar(1, 1, 5, "Muito bom");
//
//        assertEquals(5, resultado.getEstrelas());
//        assertEquals("Muito bom", resultado.getComentario());
//    }
//
//    @Test
//    void testSalvarAvaliacaoProdutoJaAvaliado() {
//        ProdutoLocacao pl = new ProdutoLocacao();
//        pl.setAvaliado(true);
//        when(produtoLocacaoRepository.findById(1)).thenReturn(Optional.of(pl));
//
//        LudiBoxException e = assertThrows(LudiBoxException.class, () ->
//                avaliacaoService.salvar(1, 1, 5, "Teste")
//        );
//        assertEquals("avaliado", e.getCampo());
//    }
//
//    @Test
//    void testAlterarAvaliacaoComSucesso() {
//        Avaliacao aval = new Avaliacao();
//        aval.setId(1);
//
//        Pessoa avaliador = new Pessoa();
//        avaliador.setId(1);
//
//        aval.setAvaliador(avaliador);
//        aval.setAtivo(true);
//
//        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(aval));
//        when(avaliacaoRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
//
//        Avaliacao resultado = avaliacaoService.alterar(1L, 4, 1L); // IDs precisam bater
//        assertEquals(4, resultado.getEstrelas());
//    }
//
//
//
//    @Test
//    void testAlterarAvaliacaoNaoPermitida() {
//        Avaliacao aval = new Avaliacao();
//        Pessoa avaliador = new Pessoa();
//        avaliador.setId(2);
//        aval.setAvaliador(avaliador);
//        aval.setAtivo(true);
//
//        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(aval));
//
//        assertThrows(LudiBoxException.class, () -> avaliacaoService.alterar(1L, 5, 1L));
//    }
//
//    @Test
//    void testDeletarAvaliacaoComSucesso() {
//        Avaliacao aval = new Avaliacao();
//        Pessoa avaliador = new Pessoa();
//        avaliador.setId(1);
//        aval.setAvaliador(avaliador);
//        aval.setAtivo(true);
//
//        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(aval));
//
//        assertDoesNotThrow(() -> avaliacaoService.deletar(1L, 1L));
//        verify(avaliacaoRepository).save(aval);
//        assertFalse(aval.getAtivo());
//    }
//
//    @Test
//    void testDeletarAvaliacaoNaoPermitida() {
//        Avaliacao aval = new Avaliacao();
//        Pessoa avaliador = new Pessoa();
//        avaliador.setId(2);
//        aval.setAvaliador(avaliador);
//        aval.setAtivo(true);
//
//        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(aval));
//
//        assertThrows(LudiBoxException.class, () -> avaliacaoService.deletar(1L, 1L));
//    }
//}

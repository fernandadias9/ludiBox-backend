package br.com.ludibox.service;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.dto.ProdutoDetalheDto;
import br.com.ludibox.model.dto.ProdutoListarDto;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.entity.Produto;
import br.com.ludibox.model.enums.EnumPerfil;
import br.com.ludibox.model.enums.StatusProduto;
import br.com.ludibox.model.repository.ProdutoRepository;
import br.com.ludibox.service.IA.ValidadorConteudoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProdutoServiceTest {

    @InjectMocks
    private ProdutoService produtoService;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private AuthenticationService authService;

    @Mock
    private ImagemService imagemService;

    @Mock
    private ValidadorConteudoService validadorConteudoService;

    @Mock
    private MultipartFile imagem;

    private Pessoa pessoaUsuario;
    private Pessoa pessoaAdmin;
    private Produto produto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        pessoaUsuario = new Pessoa();
        pessoaUsuario.setId(1);
        pessoaUsuario.setPerfil(EnumPerfil.USUARIO);
        pessoaUsuario.setNome("Usuario Teste");

        pessoaAdmin = new Pessoa();
        pessoaAdmin.setId(2);
        pessoaAdmin.setPerfil(EnumPerfil.ADMINISTRADOR);
        pessoaAdmin.setNome("Admin Teste");

        produto = new Produto();
        produto.setId(1);
        produto.setNome("Produto Teste");
        produto.setDescricao("Descrição teste");
        produto.setPreco(100.0);
        produto.setAnunciante(pessoaUsuario);
        produto.setStatus(StatusProduto.ATIVO);
        produto.setImagens(Arrays.asList("imagem1", "imagem2"));
        produto.setDataCadastro(LocalDate.from(LocalDateTime.now()));
    }

    @Test
    void testSalvarProdutoComImagemValida() throws Exception {
        Produto produto = new Produto();
        List<MultipartFile> imagens = List.of(imagem);

        when(imagem.getSize()).thenReturn(1024L);
        when(imagemService.processarImagem(any())).thenReturn("base64");
        when(authService.getPessoaAutenticada()).thenReturn(pessoaUsuario);

        produtoService.salvar(produto, imagens);

        verify(produtoRepository).save(any(Produto.class));
        verify(validadorConteudoService).validar(any());
    }

    @Test
    void testSalvarProdutoSemImagens() throws Exception {
        Produto produto = new Produto();

        when(authService.getPessoaAutenticada()).thenReturn(pessoaUsuario);

        produtoService.salvar(produto, null);

        verify(produtoRepository).save(any(Produto.class));
        verify(validadorConteudoService).validar(any());
    }

    @Test
    void testSalvarProdutoComListaImagensVazia() throws Exception {
        Produto produto = new Produto();
        List<MultipartFile> imagens = new ArrayList<>();

        when(authService.getPessoaAutenticada()).thenReturn(pessoaUsuario);

        produtoService.salvar(produto, imagens);

        verify(produtoRepository).save(any(Produto.class));
        verify(validadorConteudoService).validar(any());
    }

    @Test
    void testSalvarProdutoComMuitasImagens() {
        Produto produto = new Produto();
        List<MultipartFile> imagens = new ArrayList<>();
        for (int i = 0; i < 5; i++) imagens.add(imagem);

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> produtoService.salvar(produto, imagens));

        assertEquals("Número máximo de imagens excedido. Máximo permitido: 4", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void testSalvarProdutoComImagemMuitoGrande() throws IOException {
        Produto produto = new Produto();
        List<MultipartFile> imagens = List.of(imagem);

        when(imagem.getSize()).thenReturn(3 * 1024 * 1024L); // 3MB
        when(authService.getPessoaAutenticada()).thenReturn(pessoaUsuario);

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> produtoService.salvar(produto, imagens));

        assertTrue(exception.getMessage().contains("Tamanho máximo da imagem excedido"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void testAtualizarProdutoComSucesso() throws Exception {
        Produto produtoAtualizado = new Produto();
        produtoAtualizado.setNome("Produto Atualizado");
        produtoAtualizado.setDescricao("Nova descrição");
        produtoAtualizado.setPreco(200.0);
        produtoAtualizado.setImagens(Arrays.asList("imagem_existente"));

        when(authService.getPessoaAutenticada()).thenReturn(pessoaUsuario);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        produtoService.atualizar(1, produtoAtualizado, null);

        verify(produtoRepository).save(any(Produto.class));
    }

    @Test
    void testAtualizarProdutoComNovasImagens() throws Exception {
        Produto produtoAtualizado = new Produto();
        List<MultipartFile> novasImagens = List.of(imagem);

        when(authService.getPessoaAutenticada()).thenReturn(pessoaUsuario);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(imagem.getSize()).thenReturn(1024L);
        when(imagemService.processarImagem(any())).thenReturn("nova_imagem_base64");

        produtoService.atualizar(1, produtoAtualizado, novasImagens);

        verify(produtoRepository).save(any(Produto.class));
        verify(imagemService).processarImagem(imagem);
    }

    @Test
    void testAtualizarProdutoComImagensExistentesENovas() throws Exception {
        Produto produtoAtualizado = new Produto();
        produtoAtualizado.setImagens(Arrays.asList("imagem_existente"));
        List<MultipartFile> novasImagens = List.of(imagem);

        when(authService.getPessoaAutenticada()).thenReturn(pessoaUsuario);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(imagem.getSize()).thenReturn(1024L);
        when(imagemService.processarImagem(any())).thenReturn("nova_imagem_base64");

        produtoService.atualizar(1, produtoAtualizado, novasImagens);

        verify(produtoRepository).save(any(Produto.class));
    }

    @Test
    void testAtualizarProdutoNaoAutorizado() {
        Produto produtoAtualizado = new Produto();
        Pessoa outraPessoa = new Pessoa();
        outraPessoa.setId(999);
        outraPessoa.setPerfil(EnumPerfil.USUARIO);

        when(authService.getPessoaAutenticada()).thenReturn(outraPessoa);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> produtoService.atualizar(1, produtoAtualizado, null));

        assertEquals("Apenas o anunciante pode atualizar o anúncio.", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    void testAtualizarProdutoAdminPodeAtualizar() throws Exception {
        Produto produtoAtualizado = new Produto();
        produtoAtualizado.setNome("Atualizado por Admin");

        when(authService.getPessoaAutenticada()).thenReturn(pessoaAdmin);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        produtoService.atualizar(1, produtoAtualizado, null);

        verify(produtoRepository).save(any(Produto.class));
    }

    @Test
    void testAtualizarProdutoComMuitasImagens() {
        Produto produtoAtualizado = new Produto();
        List<MultipartFile> imagens = new ArrayList<>();
        for (int i = 0; i < 5; i++) imagens.add(imagem);

        when(authService.getPessoaAutenticada()).thenReturn(pessoaUsuario);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> produtoService.atualizar(1, produtoAtualizado, imagens));

        assertTrue(exception.getMessage().contains("Número máximo de imagens excedido"));
    }

    @Test
    void testAtualizarProdutoComImagemMuitoGrande() {
        Produto produtoAtualizado = new Produto();
        List<MultipartFile> imagens = List.of(imagem);

        when(authService.getPessoaAutenticada()).thenReturn(pessoaUsuario);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(imagem.getSize()).thenReturn(3 * 1024 * 1024L);

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> produtoService.atualizar(1, produtoAtualizado, imagens));

        assertTrue(exception.getMessage().contains("Tamanho máximo da imagem excedido"));
    }

    @Test
    void testAtualizarStatusComSucesso() throws Exception {
        when(authService.getPessoaAutenticada()).thenReturn(pessoaUsuario);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        produtoService.atualizarStatus(1, StatusProduto.INATIVO);

        verify(produtoRepository).save(any(Produto.class));
    }

    @Test
    void testAtualizarStatusProdutoBloqueado() {
        produto.setStatus(StatusProduto.BLOQUEADO);

        when(authService.getPessoaAutenticada()).thenReturn(pessoaUsuario);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> produtoService.atualizarStatus(1, StatusProduto.ATIVO));

        assertEquals("Somente administradores podem modificar o status de anúncios bloqueados", exception.getMessage());
    }

    @Test
    void testAtualizarStatusNaoAutorizado() {
        Pessoa outraPessoa = new Pessoa();
        outraPessoa.setId(999);

        when(authService.getPessoaAutenticada()).thenReturn(outraPessoa);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> produtoService.atualizarStatus(1, StatusProduto.INATIVO));

        assertEquals("Apenas o anunciante pode ativar/desativar o anúncio.", exception.getMessage());
    }

    @Test
    void testAtualizarBloqueioDesbloqueandoProduto() throws Exception {
        produto.setStatus(StatusProduto.BLOQUEADO);

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        doNothing().when(authService).verificarPermissaoAdmin();

        produtoService.atualizarBloqueio(1);

        verify(produtoRepository).save(argThat(p -> p.getStatus() == StatusProduto.ATIVO));
    }

    @Test
    void testAtualizarBloqueioBloqueandoProduto() throws Exception {
        produto.setStatus(StatusProduto.ATIVO);

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        doNothing().when(authService).verificarPermissaoAdmin();

        produtoService.atualizarBloqueio(1);

        verify(produtoRepository).save(argThat(p -> p.getStatus() == StatusProduto.BLOQUEADO));
    }

    @Test
    void testAtualizarBloqueioSemPermissao() throws Exception {
        doThrow(new LudiBoxException("Sem permissão", "Admin necessário", HttpStatus.FORBIDDEN))
                .when(authService).verificarPermissaoAdmin();

        assertThrows(LudiBoxException.class, () -> produtoService.atualizarBloqueio(1));
    }

    @Test
    void testDeletarProdutoComSucesso() throws Exception {
        when(authService.getPessoaAutenticada()).thenReturn(pessoaUsuario);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        produtoService.deletarProduto(1);

        verify(produtoRepository).delete(produto);
    }

    @Test
    void testDeletarProdutoNaoAutorizado() {
        Pessoa outraPessoa = new Pessoa();
        outraPessoa.setId(999);
        outraPessoa.setPerfil(EnumPerfil.USUARIO);

        when(authService.getPessoaAutenticada()).thenReturn(outraPessoa);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> produtoService.deletarProduto(1));

        assertEquals("Apenas o anunciante pode excluir o anúncio.", exception.getMessage());
    }

    @Test
    void testDeletarProdutoAdminPodeDeletar() throws Exception {
        when(authService.getPessoaAutenticada()).thenReturn(pessoaAdmin);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        produtoService.deletarProduto(1);

        verify(produtoRepository).delete(produto);
    }

    @Test
    void testBuscarTodosComProdutosAtivos() {
        Produto produto1 = new Produto();
        produto1.setId(1);
        produto1.setNome("Produto 1");
        produto1.setPreco(100.0);
        produto1.setStatus(StatusProduto.INATIVO); // Diferente de ATIVO
        produto1.setAnunciante(pessoaUsuario);
        produto1.setImagens(Arrays.asList("imagem1"));

        Produto produto2 = new Produto();
        produto2.setId(2);
        produto2.setNome("Produto 2");
        produto2.setPreco(200.0);
        produto2.setStatus(StatusProduto.BLOQUEADO); // Diferente de ATIVO
        produto2.setAnunciante(pessoaUsuario);
        produto2.setImagens(new ArrayList<>());

        when(produtoRepository.findByAnuncianteSituacaoTrue())
                .thenReturn(Arrays.asList(produto1, produto2));

        List<ProdutoListarDto> resultado = produtoService.buscarTodos();

        assertEquals(2, resultado.size());
        assertEquals("Produto 1", resultado.get(0).getNome());
        assertEquals("Produto 2", resultado.get(1).getNome());
        assertNull(resultado.get(1).getImagem()); // Lista vazia de imagens
    }

    @Test
    void testBuscarTodosComProdutoAtivo() {
        Produto produtoAtivo = new Produto();
        produtoAtivo.setId(1);
        produtoAtivo.setStatus(StatusProduto.ATIVO);
        produtoAtivo.setAnunciante(pessoaUsuario);

        when(produtoRepository.findByAnuncianteSituacaoTrue())
                .thenReturn(Arrays.asList(produtoAtivo));

        List<ProdutoListarDto> resultado = produtoService.buscarTodos();

        assertEquals(0, resultado.size()); // Filtro exclui produtos ATIVO
    }

    @Test
    void testBuscarProdutoDetalhe() {
        produto.setAltura(10);
        produto.setLargura(20);
        produto.setComprimento(30);
        produto.setPesoSuportado(50);
        produto.setDatasIndisponiveis(Arrays.asList(LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-02")));
        pessoaUsuario.setImagemUsuarioEmBase64("imagem_usuario");

        when(produtoRepository.findByIdAndAnuncianteAtivo(1)).thenReturn(Optional.of(produto));

        ProdutoDetalheDto dto = produtoService.buscar(1);

        assertEquals(1, dto.getId());
        assertEquals("Produto Teste", dto.getNome());
        assertEquals(10.0, dto.getAltura());
        assertEquals(20.0, dto.getLargura());
        assertEquals(30.0, dto.getComprimento());
        assertEquals(50.0, dto.getPesoSuportado());
        assertEquals("Descrição teste", dto.getDescricao());
        assertEquals(100.0, dto.getPreco());
        assertEquals(2, dto.getDatasIndisponiveis().size());
        assertEquals(1, dto.getIdAnunciante());
        assertEquals("Usuario Teste", dto.getNomeAnunciante());
        assertEquals("imagem_usuario", dto.getImagemAnunciante());
        assertEquals(2, dto.getImagens().size());
    }

    @Test
    void testBuscarProdutoNaoEncontrado() {
        when(produtoRepository.findByIdAndAnuncianteAtivo(1)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> produtoService.buscar(1));

        assertEquals("Produto não encontrado", exception.getMessage());
    }

    // TESTES PARA MÉTODO VALIDAR PRODUTO
    @Test
    void testValidarProdutoExistente() throws Exception {
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        Produto resultado = produtoService.validarProduto(1);

        assertEquals(produto, resultado);
    }

    @Test
    void testValidarProdutoNaoEncontrado() {
        when(produtoRepository.findById(1)).thenReturn(Optional.empty());

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> produtoService.validarProduto(1));

        assertEquals("Anúncio com ID 1 não encontrado", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    // TESTES PARA MÉTODO LISTAR POR USUARIO
    @Test
    void testListarPorUsuario() {
        List<Produto> produtos = Arrays.asList(produto);

        when(produtoRepository.findByAnuncianteId(1)).thenReturn(produtos);

        List<Produto> resultado = produtoService.listarPorUsuario(1);

        assertEquals(1, resultado.size());
        assertEquals(produto, resultado.get(0));
    }

    // TESTES PARA MÉTODO BUSCAR COM FILTRO
    @Test
    void testBuscarComFiltroComNome() {
        produto.setImagens(Arrays.asList("imagem1"));
        pessoaUsuario.setImagemUsuarioEmBase64("imagem_usuario");
        Page<Produto> page = new PageImpl<>(Arrays.asList(produto));

        when(produtoRepository.findByNomeContainingIgnoreCaseAndStatusAndAnuncianteAtivo(
                eq("Brin"), eq(StatusProduto.ATIVO), any(Pageable.class))).thenReturn(page);

        Page<ProdutoListarDto> resultado = produtoService.buscarComFiltro("Brin", PageRequest.of(0, 10));

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Produto Teste", resultado.getContent().get(0).getNome());
        assertEquals("imagem1", resultado.getContent().get(0).getImagem());
    }

    @Test
    void testBuscarComFiltroSemNome() {
        produto.setImagens(new ArrayList<>()); // Lista vazia
        Page<Produto> page = new PageImpl<>(Arrays.asList(produto));

        when(produtoRepository.findByStatusAndAnuncianteAtivo(
                eq(StatusProduto.ATIVO), any(Pageable.class))).thenReturn(page);

        Page<ProdutoListarDto> resultado = produtoService.buscarComFiltro(null, PageRequest.of(0, 10));

        assertEquals(1, resultado.getTotalElements());
        assertNull(resultado.getContent().get(0).getImagem()); // Imagem null quando lista vazia
    }

    @Test
    void testBuscarComFiltroNomeVazio() {
        Page<Produto> page = new PageImpl<>(Arrays.asList(produto));

        when(produtoRepository.findByStatusAndAnuncianteAtivo(
                eq(StatusProduto.ATIVO), any(Pageable.class))).thenReturn(page);

        Page<ProdutoListarDto> resultado = produtoService.buscarComFiltro("", PageRequest.of(0, 10));

        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    void testBuscarComFiltroNomeEmBranco() {
        Page<Produto> page = new PageImpl<>(Arrays.asList(produto));

        when(produtoRepository.findByStatusAndAnuncianteAtivo(
                eq(StatusProduto.ATIVO), any(Pageable.class))).thenReturn(page);

        Page<ProdutoListarDto> resultado = produtoService.buscarComFiltro("   ", PageRequest.of(0, 10));

        assertEquals(1, resultado.getTotalElements());
    }
}
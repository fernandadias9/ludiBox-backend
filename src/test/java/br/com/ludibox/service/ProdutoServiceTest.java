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
    private MultipartFile imagem1;

    @Mock
    private MultipartFile imagem2;

    private Produto produto;
    private Pessoa anunciante;
    private Pessoa admin;
    private Pessoa outroUsuario;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        anunciante = new Pessoa();
        anunciante.setId(1);
        anunciante.setNome("João Anunciante");
        anunciante.setPerfil(EnumPerfil.USUARIO);
        anunciante.setSituacao(true);
        anunciante.setImagemUsuarioEmBase64("imagemAnunciante");

        admin = new Pessoa();
        admin.setId(2);
        admin.setNome("Admin User");
        admin.setPerfil(EnumPerfil.ADMINISTRADOR);
        admin.setSituacao(true);

        outroUsuario = new Pessoa();
        outroUsuario.setId(3);
        outroUsuario.setNome("Outro Usuario");
        outroUsuario.setPerfil(EnumPerfil.USUARIO);
        outroUsuario.setSituacao(true);

        produto = new Produto();
        produto.setId(1);
        produto.setNome("Produto Teste");
        produto.setDescricao("Descrição do produto teste");
        produto.setPreco(100.0);
        produto.setAltura(10);
        produto.setLargura(20);
        produto.setComprimento(30);
        produto.setPesoSuportado(50);
        produto.setAnunciante(anunciante);
        produto.setStatus(StatusProduto.ATIVO);
        produto.setDataCadastro(LocalDate.now());
        produto.setImagens(Arrays.asList("imagem1", "imagem2"));
        produto.setDatasIndisponiveis(new ArrayList<>());
    }

    // ========== TESTES PARA salvar ==========

    @Test
    void testSalvar_ComSucesso() throws Exception {
        List<MultipartFile> imagens = Arrays.asList(imagem1, imagem2);

        when(authService.getPessoaAutenticada()).thenReturn(anunciante);
        when(imagem1.getSize()).thenReturn(1024L);
        when(imagem2.getSize()).thenReturn(2048L);
        when(imagemService.processarImagem(imagem1)).thenReturn("base64_1");
        when(imagemService.processarImagem(imagem2)).thenReturn("base64_2");
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        produtoService.salvar(produto, imagens);

        verify(produtoRepository).save(any(Produto.class));
        verify(validadorConteudoService).validar(produto);
        verify(imagemService, times(2)).processarImagem(any());
        assertEquals(anunciante, produto.getAnunciante());
    }

    @Test
    void testSalvar_SemImagens_ComSucesso() throws Exception {
        when(authService.getPessoaAutenticada()).thenReturn(anunciante);
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        produtoService.salvar(produto, null);

        verify(produtoRepository).save(any(Produto.class));
        verify(validadorConteudoService).validar(produto);
        verify(imagemService, never()).processarImagem(any());
    }

    @Test
    void testSalvar_MuitasImagens_DeveLancarExcecao() {
        List<MultipartFile> imagens = Arrays.asList(imagem1, imagem2, imagem1, imagem2, imagem1); // 5 imagens

        when(authService.getPessoaAutenticada()).thenReturn(anunciante);

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> produtoService.salvar(produto, imagens));

        assertEquals("Imagens", exception.getCampo());
        assertEquals("Número máximo de imagens excedido. Máximo permitido: 4", exception.getMensagem());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void testSalvar_ImagemMuitoGrande_DeveLancarExcecao() {
        List<MultipartFile> imagens = Arrays.asList(imagem1);

        when(authService.getPessoaAutenticada()).thenReturn(anunciante);
        when(imagem1.getSize()).thenReturn(3 * 1024 * 1024L); // 3MB

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> produtoService.salvar(produto, imagens));

        assertEquals("Imagens", exception.getCampo());
        assertTrue(exception.getMensagem().contains("Tamanho máximo da imagem excedido"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }


    @Test
    void testAtualizar_ComSucesso() throws Exception {
        Produto produtoAtualizado = new Produto();
        produtoAtualizado.setNome("Produto Atualizado");
        produtoAtualizado.setDescricao("Nova descrição");
        produtoAtualizado.setPreco(150.0);
        produtoAtualizado.setStatus(StatusProduto.ATIVO);

        when(authService.getPessoaAutenticada()).thenReturn(anunciante);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        produtoService.atualizar(1, produtoAtualizado, null);

        verify(produtoRepository).save(any(Produto.class));
        assertEquals("Produto Atualizado", produto.getNome());
        assertEquals("Nova descrição", produto.getDescricao());
        assertEquals(150.0, produto.getPreco());
    }

    @Test
    void testAtualizar_ComNovasImagens_ComSucesso() throws Exception {
        Produto produtoAtualizado = new Produto();
        produtoAtualizado.setNome("Produto Atualizado");
        List<MultipartFile> novasImagens = Arrays.asList(imagem1);

        when(authService.getPessoaAutenticada()).thenReturn(anunciante);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(imagem1.getSize()).thenReturn(1024L);
        when(imagemService.processarImagem(imagem1)).thenReturn("nova_imagem_base64");
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        produtoService.atualizar(1, produtoAtualizado, novasImagens);

        verify(produtoRepository).save(any(Produto.class));
        verify(imagemService).processarImagem(imagem1);
    }

    @Test
    void testAtualizar_UsuarioNaoAutorizado_DeveLancarExcecao() {
        Produto produtoAtualizado = new Produto();

        when(authService.getPessoaAutenticada()).thenReturn(outroUsuario);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> produtoService.atualizar(1, produtoAtualizado, null));

        assertEquals("Atualização não permitida", exception.getCampo());
        assertEquals("Apenas o anunciante pode atualizar o anúncio.", exception.getMensagem());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    void testAtualizar_AdminPodeAtualizar_ComSucesso() throws Exception {
        Produto produtoAtualizado = new Produto();
        produtoAtualizado.setNome("Produto Atualizado pelo Admin");

        when(authService.getPessoaAutenticada()).thenReturn(admin);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        produtoService.atualizar(1, produtoAtualizado, null);

        verify(produtoRepository).save(any(Produto.class));
        assertEquals("Produto Atualizado pelo Admin", produto.getNome());
    }

    @Test
    void testAtualizar_ProdutoNaoEncontrado_DeveLancarExcecao() {
        Produto produtoAtualizado = new Produto();

        when(authService.getPessoaAutenticada()).thenReturn(anunciante);
        when(produtoRepository.findById(1)).thenReturn(Optional.empty());

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> produtoService.atualizar(1, produtoAtualizado, null));

        assertEquals("Anúncio não encontrado", exception.getCampo());
        assertEquals("Anúncio com ID 1 não encontrado", exception.getMensagem());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    void testAtualizar_MuitasImagens_DeveLancarExcecao() {
        Produto produtoAtualizado = new Produto();
        List<MultipartFile> imagens = Arrays.asList(imagem1, imagem2, imagem1, imagem2, imagem1); // 5 imagens

        when(authService.getPessoaAutenticada()).thenReturn(anunciante);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> produtoService.atualizar(1, produtoAtualizado, imagens));

        assertEquals("Imagens", exception.getCampo());
        assertEquals("Número máximo de imagens excedido. Máximo permitido: 4", exception.getMensagem());
    }

    // ========== TESTES PARA atualizarStatus ==========

    @Test
    void testAtualizarStatus_ComSucesso() {
        when(authService.getPessoaAutenticada()).thenReturn(anunciante);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        produtoService.atualizarStatus(1, StatusProduto.INATIVO);

        verify(produtoRepository).save(any(Produto.class));
        assertEquals(StatusProduto.INATIVO, produto.getStatus());
    }

    @Test
    void testAtualizarStatus_UsuarioNaoAutorizado_DeveLancarExcecao() {
        when(authService.getPessoaAutenticada()).thenReturn(outroUsuario);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> produtoService.atualizarStatus(1, StatusProduto.INATIVO));

        assertEquals("Ação não autorizada.", exception.getCampo());
        assertEquals("Apenas o anunciante pode ativar/desativar o anúncio.", exception.getMensagem());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    void testAtualizarStatus_ProdutoBloqueado_DeveLancarExcecao() {
        produto.setStatus(StatusProduto.BLOQUEADO);

        when(authService.getPessoaAutenticada()).thenReturn(anunciante);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> produtoService.atualizarStatus(1, StatusProduto.ATIVO));

        assertEquals("Ação inválida", exception.getCampo());
        assertEquals("Somente administradores podem modificar o status de anúncios bloqueados", exception.getMensagem());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    void testAtualizarStatus_ProdutoNaoEncontrado_DeveLancarExcecao() {
        when(authService.getPessoaAutenticada()).thenReturn(anunciante);
        when(produtoRepository.findById(1)).thenReturn(Optional.empty());

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> produtoService.atualizarStatus(1, StatusProduto.INATIVO));

        assertEquals("Anúncio não encontrado", exception.getCampo());
        assertEquals("Anúncio com ID 1 não encontrado", exception.getMensagem());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    void testAtualizarBloqueio_SemPermissaoAdmin_DeveLancarExcecao() {
        doThrow(new LudiBoxException("Permissão", "Acesso negado", HttpStatus.UNAUTHORIZED))
                .when(authService).verificarPermissaoAdmin();

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> produtoService.atualizarBloqueio(1));

        assertEquals("Permissão", exception.getCampo());
        assertEquals("Acesso negado", exception.getMensagem());
    }

    @Test
    void testDeletarProduto_AdminPodeDeletar_ComSucesso() {
        when(authService.getPessoaAutenticada()).thenReturn(admin);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        produtoService.deletarProduto(1);

        verify(produtoRepository).delete(produto);
    }

    @Test
    void testDeletarProduto_UsuarioNaoAutorizado_DeveLancarExcecao() {
        when(authService.getPessoaAutenticada()).thenReturn(outroUsuario);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> produtoService.deletarProduto(1));

        assertEquals("Exclusão não permitida", exception.getCampo());
        assertEquals("Apenas o anunciante pode excluir o anúncio.", exception.getMensagem());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    void testDeletarProduto_ProdutoNaoEncontrado_DeveLancarExcecao() {
        when(authService.getPessoaAutenticada()).thenReturn(anunciante);
        when(produtoRepository.findById(1)).thenReturn(Optional.empty());

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> produtoService.deletarProduto(1));

        assertEquals("Anúncio não encontrado", exception.getCampo());
        assertEquals("Anúncio com ID 1 não encontrado", exception.getMensagem());
    }

    @Test
    void testBuscarTodos_ListaVazia() {
        when(produtoRepository.findByAnuncianteSituacaoTrue()).thenReturn(new ArrayList<>());

        List<ProdutoListarDto> resultado = produtoService.buscarTodos();

        assertTrue(resultado.isEmpty());
    }

    @Test
    void testBuscarTodos_ProdutoSemImagem() {
        produto.setImagens(new ArrayList<>());
        produto.setStatus(StatusProduto.INATIVO);

        when(produtoRepository.findByAnuncianteSituacaoTrue()).thenReturn(Arrays.asList(produto));

        List<ProdutoListarDto> resultado = produtoService.buscarTodos();

        assertEquals(1, resultado.size());
        assertNull(resultado.get(0).getImagem());
    }

    // ========== TESTES PARA buscar ==========

    @Test
    void testBuscar_ComSucesso() {
        when(produtoRepository.findByIdAndAnuncianteAtivo(1)).thenReturn(Optional.of(produto));

        ProdutoDetalheDto resultado = produtoService.buscar(1);

        assertEquals(1, resultado.getId());
        assertEquals("Produto Teste", resultado.getNome());
        assertEquals("Descrição do produto teste", resultado.getDescricao());
        assertEquals(100.0, resultado.getPreco());
        assertEquals(10.0, resultado.getAltura());
        assertEquals(20.0, resultado.getLargura());
        assertEquals(30.0, resultado.getComprimento());
        assertEquals(50.0, resultado.getPesoSuportado());
        assertEquals(1, resultado.getIdAnunciante());
        assertEquals("João Anunciante", resultado.getNomeAnunciante());
        assertEquals("imagemAnunciante", resultado.getImagemAnunciante());
        assertEquals(Arrays.asList("imagem1", "imagem2"), resultado.getImagens());
        assertNotNull(resultado.getDataCadastro());
        assertNotNull(resultado.getDatasIndisponiveis());
    }

    @Test
    void testBuscar_ProdutoNaoEncontrado_DeveLancarExcecao() {
        when(produtoRepository.findByIdAndAnuncianteAtivo(1)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> produtoService.buscar(1));

        assertEquals("Produto não encontrado", exception.getMessage());
    }

    // ========== TESTES PARA validarProduto ==========

    @Test
    void testValidarProduto_ComSucesso() {
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        Produto resultado = produtoService.validarProduto(1);

        assertEquals(produto, resultado);
    }

    @Test
    void testValidarProduto_ProdutoNaoEncontrado_DeveLancarExcecao() {
        when(produtoRepository.findById(1)).thenReturn(Optional.empty());

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> produtoService.validarProduto(1));

        assertEquals("Anúncio não encontrado", exception.getCampo());
        assertEquals("Anúncio com ID 1 não encontrado", exception.getMensagem());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    // ========== TESTES PARA listarPorUsuario ==========

    @Test
    void testListarPorUsuario_ComSucesso() {
        List<Produto> produtos = Arrays.asList(produto);

        when(produtoRepository.findByAnuncianteId(1)).thenReturn(produtos);

        List<Produto> resultado = produtoService.listarPorUsuario(1);

        assertEquals(1, resultado.size());
        assertEquals(produto, resultado.get(0));
    }

    @Test
    void testListarPorUsuario_ListaVazia() {
        when(produtoRepository.findByAnuncianteId(1)).thenReturn(new ArrayList<>());

        List<Produto> resultado = produtoService.listarPorUsuario(1);

        assertTrue(resultado.isEmpty());
    }

    // ========== TESTES PARA buscarComFiltro ==========

    @Test
    void testBuscarComFiltro_ComNome_ComSucesso() {
        Page<Produto> page = new PageImpl<>(Arrays.asList(produto));
        Pageable pageable = PageRequest.of(0, 10);

        when(produtoRepository.findByNomeContainingIgnoreCaseAndStatusAndAnuncianteAtivo(
                "Produto", StatusProduto.ATIVO, pageable)).thenReturn(page);

        Page<ProdutoListarDto> resultado = produtoService.buscarComFiltro("Produto", pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Produto Teste", resultado.getContent().get(0).getNome());
        assertEquals(1, resultado.getContent().get(0).getId());
        assertEquals(100.0, resultado.getContent().get(0).getPreco());
        assertEquals(1, resultado.getContent().get(0).getIdAnunciante());
        assertEquals("João Anunciante", resultado.getContent().get(0).getNomeAnunciante());
        assertEquals("imagemAnunciante", resultado.getContent().get(0).getImagemAnunciante());
        assertEquals("imagem1", resultado.getContent().get(0).getImagem());
    }

    @Test
    void testBuscarComFiltro_NomeVazio_ComSucesso() {
        Page<Produto> page = new PageImpl<>(Arrays.asList(produto));
        Pageable pageable = PageRequest.of(0, 10);

        when(produtoRepository.findByStatusAndAnuncianteAtivo(StatusProduto.ATIVO, pageable))
                .thenReturn(page);

        Page<ProdutoListarDto> resultado = produtoService.buscarComFiltro("", pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Produto Teste", resultado.getContent().get(0).getNome());
    }

    @Test
    void testBuscarComFiltro_NomeEmBranco_ComSucesso() {
        Page<Produto> page = new PageImpl<>(Arrays.asList(produto));
        Pageable pageable = PageRequest.of(0, 10);

        when(produtoRepository.findByStatusAndAnuncianteAtivo(StatusProduto.ATIVO, pageable))
                .thenReturn(page);

        Page<ProdutoListarDto> resultado = produtoService.buscarComFiltro("   ", pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Produto Teste", resultado.getContent().get(0).getNome());
    }

    @Test
    void testBuscarComFiltro_ProdutoSemImagem() {
        produto.setImagens(new ArrayList<>());
        Page<Produto> page = new PageImpl<>(Arrays.asList(produto));
        Pageable pageable = PageRequest.of(0, 10);

        when(produtoRepository.findByStatusAndAnuncianteAtivo(StatusProduto.ATIVO, pageable))
                .thenReturn(page);

        Page<ProdutoListarDto> resultado = produtoService.buscarComFiltro(null, pageable);

        assertEquals(1, resultado.getTotalElements());
        assertNull(resultado.getContent().get(0).getImagem());
    }

    @Test
    void testBuscarComFiltro_ResultadoVazio() {
        Page<Produto> page = new PageImpl<>(new ArrayList<>());
        Pageable pageable = PageRequest.of(0, 10);

        when(produtoRepository.findByNomeContainingIgnoreCaseAndStatusAndAnuncianteAtivo(
                "Inexistente", StatusProduto.ATIVO, pageable)).thenReturn(page);

        Page<ProdutoListarDto> resultado = produtoService.buscarComFiltro("Inexistente", pageable);

        assertEquals(0, resultado.getTotalElements());
        assertTrue(resultado.getContent().isEmpty());
    }

    // ========== TESTES ADICIONAIS DE INTEGRAÇÃO ==========

    @Test
    void testFluxoCompleto_CriarAtualizarExcluir() throws Exception {
        // Criar produto
        List<MultipartFile> imagens = Arrays.asList(imagem1);

        when(authService.getPessoaAutenticada()).thenReturn(anunciante);
        when(imagem1.getSize()).thenReturn(1024L);
        when(imagemService.processarImagem(imagem1)).thenReturn("base64_1");
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        produtoService.salvar(produto, imagens);
        verify(produtoRepository).save(any(Produto.class));

        // Atualizar produto
        Produto produtoAtualizado = new Produto();
        produtoAtualizado.setNome("Produto Atualizado");

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        produtoService.atualizar(1, produtoAtualizado, null);
        verify(produtoRepository, times(2)).save(any(Produto.class));

        // Excluir produto
        produtoService.deletarProduto(1);
        verify(produtoRepository).delete(produto);
    }

    @Test
    void testValidacaoTamanhoImagem_LimiteExato() throws Exception {
        List<MultipartFile> imagens = Arrays.asList(imagem1);

        when(authService.getPessoaAutenticada()).thenReturn(anunciante);
        when(imagem1.getSize()).thenReturn(2 * 1024 * 1024L); // Exatamente 2MB
        when(imagemService.processarImagem(imagem1)).thenReturn("base64_1");
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        assertDoesNotThrow(() -> produtoService.salvar(produto, imagens));
        verify(produtoRepository).save(any(Produto.class));
    }

    @Test
    void testValidacaoQuantidadeImagens_LimiteExato() throws Exception {
        List<MultipartFile> imagens = Arrays.asList(imagem1, imagem2, imagem1, imagem2); // Exatamente 4 imagens

        when(authService.getPessoaAutenticada()).thenReturn(anunciante);
        when(imagem1.getSize()).thenReturn(1024L);
        when(imagem2.getSize()).thenReturn(1024L);
        when(imagemService.processarImagem(any())).thenReturn("base64");
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        assertDoesNotThrow(() -> produtoService.salvar(produto, imagens));
        verify(produtoRepository).save(any(Produto.class));
        verify(imagemService, times(4)).processarImagem(any());
    }
}
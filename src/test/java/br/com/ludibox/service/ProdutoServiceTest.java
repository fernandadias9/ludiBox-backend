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
import org.springframework.web.multipart.MultipartFile;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSalvarProdutoComImagemValida() throws Exception {
        Produto produto = new Produto();
        Pessoa pessoa = new Pessoa();
        List<MultipartFile> imagens = List.of(imagem);

        when(imagem.getSize()).thenReturn(1024L);
        when(imagemService.processarImagem(any())).thenReturn("base64");
        when(authService.getPessoaAutenticada()).thenReturn(pessoa);

        produtoService.salvar(produto, imagens);

        verify(produtoRepository).save(any(Produto.class));
        verify(validadorConteudoService).validar(any());
    }

    @Test
    void testSalvarProdutoComMuitasImagens() {
        Produto produto = new Produto();
        List<MultipartFile> imagens = new ArrayList<>();
        for (int i = 0; i < 5; i++) imagens.add(imagem);

        assertThrows(LudiBoxException.class, () -> produtoService.salvar(produto, imagens));
    }

    @Test
    void testAtualizarProdutoNaoAutorizado() {
        Produto produto = new Produto();
        produto.setAnunciante(new Pessoa());
        produto.setId(1);

        Produto produtoAtualizado = new Produto();
        produtoAtualizado.setAnunciante(new Pessoa());
        produtoAtualizado.setId(1);

        Pessoa pessoa = new Pessoa();
        pessoa.setPerfil(EnumPerfil.USUARIO);

        when(authService.getPessoaAutenticada()).thenReturn(pessoa);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        assertThrows(LudiBoxException.class, () -> produtoService.atualizar(1, produtoAtualizado, null));
    }

    @Test
    void testAtualizarStatusProdutoBloqueado() {
        Produto produto = new Produto();
        produto.setAnunciante(new Pessoa());
        produto.setStatus(StatusProduto.BLOQUEADO);

        when(authService.getPessoaAutenticada()).thenReturn(produto.getAnunciante());
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        assertThrows(LudiBoxException.class, () -> produtoService.atualizarStatus(1, StatusProduto.ATIVO));
    }

    @Test
    void testBuscarProdutoDetalhe() {
        Produto produto = new Produto();
        produto.setId(1);
        produto.setNome("Produto Teste");
        produto.setAnunciante(new Pessoa());

        when(produtoRepository.findByIdAndAnuncianteAtivo(1)).thenReturn(Optional.of(produto));

        ProdutoDetalheDto dto = produtoService.buscar(1);
        assertEquals(1, dto.getId());
        assertEquals("Produto Teste", dto.getNome());
    }

    @Test
    void testValidarProdutoNaoEncontrado() {
        when(produtoRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(LudiBoxException.class, () -> produtoService.validarProduto(1));
    }

    @Test
    void testBuscarComFiltroNome() {
        Produto produto = new Produto();
        produto.setId(1);
        produto.setNome("Brinquedo");
        produto.setAnunciante(new Pessoa());

        Page<Produto> page = new PageImpl<>(List.of(produto));

        when(produtoRepository.findByNomeContainingIgnoreCaseAndStatusAndAnuncianteAtivo(anyString(), any(), any())).thenReturn(page);

        Page<ProdutoListarDto> resultado = produtoService.buscarComFiltro("Brin", PageRequest.of(0, 10));
        assertEquals(1, resultado.getTotalElements());
    }
}
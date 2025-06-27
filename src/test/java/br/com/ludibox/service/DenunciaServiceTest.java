package br.com.ludibox.service;

import br.com.ludibox.model.dto.DenunciaDTO;
import br.com.ludibox.model.entity.Denuncia;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.entity.Produto;
import br.com.ludibox.model.enums.EnumMotivoDenuncia;
import br.com.ludibox.model.enums.EnumStatusDenuncia;
import br.com.ludibox.model.enums.EnumStatusProdutoDenunciado;
import br.com.ludibox.model.repository.DenunciaRepository;
import br.com.ludibox.model.repository.PessoaRepository;
import br.com.ludibox.model.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DenunciaServiceTest {

    @Mock
    private DenunciaRepository denunciaRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @InjectMocks
    private DenunciaService denunciaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCriarDenuncia_ComSucesso() {
        // Arrange
        DenunciaDTO dto = new DenunciaDTO();
        dto.setProdutoId(1);
        dto.setDenuncianteId(2);
        dto.setMotivo(EnumMotivoDenuncia.OUTRO);
        dto.setDescricao("Produto chegou quebrado");

        Produto produto = new Produto();
        produto.setId(1);

        Pessoa pessoa = new Pessoa();
        pessoa.setId(2);

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(pessoaRepository.findById(2)).thenReturn(Optional.of(pessoa));

        Denuncia denunciaSalva = new Denuncia();
        denunciaSalva.setProduto(produto);
        denunciaSalva.setDenunciante(pessoa);
        denunciaSalva.setMotivo(dto.getMotivo());
        denunciaSalva.setDescricao(dto.getDescricao());
        denunciaSalva.setStatus(EnumStatusDenuncia.NOVO);
        denunciaSalva.setStatusProdutoDenunciado(EnumStatusProdutoDenunciado.PENDENTE);

        when(denunciaRepository.save(any())).thenReturn(denunciaSalva);

        // Act
        Denuncia resultado = denunciaService.criar(dto);

        // Assert
        assertNotNull(resultado);
        assertEquals(dto.getMotivo(), resultado.getMotivo());
        assertEquals(dto.getDescricao(), resultado.getDescricao());
        assertEquals(produto, resultado.getProduto());
        assertEquals(pessoa, resultado.getDenunciante());
        assertEquals(EnumStatusDenuncia.NOVO, resultado.getStatus());
        assertEquals(EnumStatusProdutoDenunciado.PENDENTE, resultado.getStatusProdutoDenunciado());
    }

    @Test
    void testCriarDenuncia_ProdutoNaoEncontrado() {
        DenunciaDTO dto = new DenunciaDTO();
        dto.setProdutoId(10);
        dto.setDenuncianteId(2);

        when(produtoRepository.findById(10)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            denunciaService.criar(dto);
        });

        assertTrue(ex.getMessage().contains("Produto não encontrado com ID"));
    }

    @Test
    void testCriarDenuncia_DenuncianteNaoEncontrado() {
        DenunciaDTO dto = new DenunciaDTO();
        dto.setProdutoId(1);
        dto.setDenuncianteId(999);

        Produto produto = new Produto();
        produto.setId(1);

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(pessoaRepository.findById(999)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            denunciaService.criar(dto);
        });

        assertTrue(ex.getMessage().contains("Denunciante não encontrado com ID"));
    }
}

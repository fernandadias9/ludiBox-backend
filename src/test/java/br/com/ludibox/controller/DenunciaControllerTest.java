package br.com.ludibox.controller;

import br.com.ludibox.model.dto.DenunciaDTO;
import br.com.ludibox.model.dto.DenunciaListarDTO;
import br.com.ludibox.model.entity.Denuncia;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.entity.Produto;
import br.com.ludibox.model.enums.*;
import br.com.ludibox.model.repository.DenunciaRepository;
import br.com.ludibox.model.repository.ProdutoRepository;
import br.com.ludibox.service.DenunciaService;
import br.com.ludibox.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(DenunciaController.class)
class DenunciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DenunciaRepository denunciaRepository;

    @MockBean
    private ProdutoRepository produtoRepository;

    @MockBean
    private DenunciaService denunciaService;

    @MockBean
    private EmailService emailService;

    private Denuncia denuncia;
    private Produto produto;
    private Pessoa anunciante;

    @BeforeEach
    void setup() {
        anunciante = new Pessoa();
        anunciante.setId(1);
        anunciante.setNome("Anunciante Teste");
        anunciante.setEmail("email@teste.com");
        anunciante.setPerfil(EnumPerfil.ADMINISTRADOR);

        produto = new Produto();
        produto.setId(100);
        produto.setNome("Produto Teste");
        produto.setAnunciante(anunciante);
        produto.setStatus(StatusProduto.BLOQUEADO);

        denuncia = new Denuncia();
        denuncia.setId(1);
        denuncia.setMotivo(EnumMotivoDenuncia.OUTRO);
        denuncia.setDescricao("Descrição da denúncia");
        denuncia.setStatus(EnumStatusDenuncia.NOVO);
        denuncia.setStatusProdutoDenunciado(EnumStatusProdutoDenunciado.BLOQUEADO);
        denuncia.setDataCriacao(LocalDateTime.now());
        denuncia.setProduto(produto);
        denuncia.setDenunciante(null);
    }

    @Test
    void criarDenuncia_deveRetornarDenunciaSalva() throws Exception {
        DenunciaDTO dto = new DenunciaDTO();
        // preencha dto conforme necessário, aqui só um exemplo básico
        dto.setDescricao("Denúncia teste");
        dto.setMotivo(EnumMotivoDenuncia.OUTRO);

        when(denunciaService.criar(any(DenunciaDTO.class))).thenReturn(denuncia);

        mockMvc.perform(post("/denuncias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(denuncia.getId()))
                .andExpect(jsonPath("$.descricao").value(denuncia.getDescricao()))
                .andExpect(jsonPath("$.motivo").value(denuncia.getMotivo().name()));
    }

    @Test
    void listarTodas_deveRetornarListaDenuncias() throws Exception {
        when(denunciaRepository.findAll()).thenReturn(List.of(denuncia));

        mockMvc.perform(get("/denuncias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(denuncia.getId()))
                .andExpect(jsonPath("$[0].motivo").value(denuncia.getMotivo().name()));
    }

    @Test
    void filtrar_deveRetornarListaFiltrada() throws Exception {
        when(denunciaRepository.buscarComFiltros(
                nullable(LocalDateTime.class),
                nullable(LocalDateTime.class),
                nullable(EnumMotivoDenuncia.class),
                nullable(EnumStatusDenuncia.class)
        )).thenReturn(List.of(denuncia));


        mockMvc.perform(get("/denuncias/filtro")
                        .param("dataInicio", "2025-06-20")
                        .param("dataFim", "2025-06-25")
                        .param("motivo", "OUTRO")
                        .param("status", "NOVO")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(denuncia.getId()))
                .andExpect(jsonPath("$[0].motivo").value(denuncia.getMotivo().name()));
    }

    @Test
    void permitir_deveAtualizarDenunciaEProduto() throws Exception {
        when(denunciaRepository.findById(denuncia.getId())).thenReturn(Optional.of(denuncia));
        when(produtoRepository.save(produto)).thenReturn(produto);
        when(denunciaRepository.save(denuncia)).thenReturn(denuncia);

        mockMvc.perform(put("/denuncias/{id}/permitir", denuncia.getId()))
                .andExpect(status().isOk());

        // Verifica que status do produto e da denúncia foram atualizados corretamente
        verify(produtoRepository).save(produto);
        verify(denunciaRepository).save(denuncia);
        assert(denuncia.getStatusProdutoDenunciado() == EnumStatusProdutoDenunciado.PERMITIDO);
        assert(denuncia.getStatus() == EnumStatusDenuncia.ANALISADO);
        assert(produto.getStatus() == StatusProduto.ATIVO);
    }

    @Test
    void permitir_deveRetornarNotFoundSeDenunciaNaoExistir() throws Exception {
        when(denunciaRepository.findById(anyInt())).thenReturn(Optional.empty());

        mockMvc.perform(put("/denuncias/{id}/permitir", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void bloquear_deveAtualizarDenunciaEProdutoEEnviarEmail() throws Exception {
        when(denunciaRepository.findById(denuncia.getId())).thenReturn(Optional.of(denuncia));
        when(produtoRepository.save(produto)).thenReturn(produto);
        when(denunciaRepository.save(denuncia)).thenReturn(denuncia);

        mockMvc.perform(put("/denuncias/{id}/bloquear", denuncia.getId()))
                .andExpect(status().isOk());

        verify(produtoRepository).save(produto);
        verify(denunciaRepository).save(denuncia);
        verify(emailService).enviarAnuncioBloqueado(anunciante.getEmail(), produto.getNome());

        assert(denuncia.getStatusProdutoDenunciado() == EnumStatusProdutoDenunciado.BLOQUEADO);
        assert(denuncia.getStatus() == EnumStatusDenuncia.ANALISADO);
        assert(produto.getStatus() == StatusProduto.BLOQUEADO);
    }

    @Test
    void bloquear_deveRetornarNotFoundSeDenunciaNaoExistir() throws Exception {
        when(denunciaRepository.findById(anyInt())).thenReturn(Optional.empty());

        mockMvc.perform(put("/denuncias/{id}/bloquear", 999))
                .andExpect(status().isNotFound());

        verifyNoInteractions(produtoRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    void filtrar_deveRetornarListaComProdutoNuloEAnuncianteNulo() throws Exception {
        // Denúncia com produto null
        Denuncia denunciaProdutoNulo = new Denuncia();
        denunciaProdutoNulo.setId(2);
        denunciaProdutoNulo.setMotivo(EnumMotivoDenuncia.CONTEUDO_INDEVIDO);
        denunciaProdutoNulo.setDescricao("Denúncia sem produto");
        denunciaProdutoNulo.setStatus(EnumStatusDenuncia.NOVO);
        denunciaProdutoNulo.setDataCriacao(LocalDateTime.now());
        denunciaProdutoNulo.setProduto(null);
        denunciaProdutoNulo.setDenunciante(null);

        // Denúncia com produto presente, mas anunciante null
        Produto produtoSemAnunciante = new Produto();
        produtoSemAnunciante.setId(200);
        produtoSemAnunciante.setNome("Produto Sem Anunciante");
        produtoSemAnunciante.setAnunciante(null);

        Denuncia denunciaAnuncianteNulo = new Denuncia();
        denunciaAnuncianteNulo.setId(3);
        denunciaAnuncianteNulo.setMotivo(EnumMotivoDenuncia.PRECO_ABUSIVO);
        denunciaAnuncianteNulo.setDescricao("Denúncia com produto sem anunciante");
        denunciaAnuncianteNulo.setStatus(EnumStatusDenuncia.NOVO);
        denunciaAnuncianteNulo.setDataCriacao(LocalDateTime.now());
        denunciaAnuncianteNulo.setProduto(produtoSemAnunciante);
        denunciaAnuncianteNulo.setDenunciante(null);

        when(denunciaRepository.buscarComFiltros(
                nullable(LocalDateTime.class),
                nullable(LocalDateTime.class),
                nullable(EnumMotivoDenuncia.class),
                nullable(EnumStatusDenuncia.class)
        )).thenReturn(List.of(denunciaProdutoNulo, denunciaAnuncianteNulo));

        mockMvc.perform(get("/denuncias/filtro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].produtoId").doesNotExist())
                .andExpect(jsonPath("$[0].nomeDenunciado").doesNotExist())
                .andExpect(jsonPath("$[1].produtoId").value(produtoSemAnunciante.getId()))
                .andExpect(jsonPath("$[1].nomeDenunciado").doesNotExist());
    }

    @Test
    void bloquear_naoDeveEnviarEmailSeEmailOuNomeProdutoNull() throws Exception {
        // Cenário 1: produto com anunciante nulo (email null)
        produto.setAnunciante(null);

        when(denunciaRepository.findById(denuncia.getId())).thenReturn(Optional.of(denuncia));
        when(produtoRepository.save(produto)).thenReturn(produto);
        when(denunciaRepository.save(denuncia)).thenReturn(denuncia);

        mockMvc.perform(put("/denuncias/{id}/bloquear", denuncia.getId()))
                .andExpect(status().isOk());

        verify(produtoRepository).save(produto);
        verify(denunciaRepository).save(denuncia);

        // Garantir que emailService não é chamado porque email é null
        verify(emailService, never()).enviarAnuncioBloqueado(anyString(), anyString());

        // Cenário 2: produto com nome null
        produto.setAnunciante(anunciante);
        produto.setNome(null);

        mockMvc.perform(put("/denuncias/{id}/bloquear", denuncia.getId()))
                .andExpect(status().isOk());

        verify(emailService, never()).enviarAnuncioBloqueado(anyString(), anyString());
    }

}

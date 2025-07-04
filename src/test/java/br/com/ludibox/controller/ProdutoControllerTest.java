package br.com.ludibox.controller;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.dto.ProdutoDetalheDto;
import br.com.ludibox.model.dto.ProdutoListarDto;
import br.com.ludibox.model.entity.Produto;
import br.com.ludibox.model.enums.StatusProduto;
import br.com.ludibox.service.ProdutoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ProdutoController.class)
public class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProdutoService produtoService;

    @Autowired
    private ObjectMapper objectMapper;

//    @Test
//    void criarProduto_deveRetornarCreated_quandoSucesso() throws Exception {
//        Produto produto = new Produto();
//        produto.setNome("Produto Teste");
//
//        MockMultipartFile produtoPart = new MockMultipartFile("produto", "", "application/json",
//                objectMapper.writeValueAsBytes(produto));
//        MockMultipartFile imagemPart = new MockMultipartFile("imagens", "imagem.jpg", "image/jpeg",
//                "imagem".getBytes());
//
//        mockMvc.perform(multipart("/produto")
//                        .file(produtoPart)
//                        .file(imagemPart)
//                        .contentType(MediaType.MULTIPART_FORM_DATA))
//                .andExpect(status().isCreated())
//                .andExpect(content().string("Anúncio criado com sucesso"));
//    }
//
//    @Test
//    void criarProduto_deveRetornarBadRequest_quandoLudiBoxException() throws Exception {
//        Produto produto = new Produto();
//        produto.setNome("Produto Teste");
//
//        MockMultipartFile produtoPart = new MockMultipartFile("produto", "", "application/json",
//                objectMapper.writeValueAsBytes(produto));
//        MockMultipartFile imagemPart = new MockMultipartFile("imagens", "imagem.jpg", "image/jpeg",
//                "imagem".getBytes());
//
//        doThrow(new LudiBoxException("Erro", "Erro ao salvar", null))
//                .when(produtoService).salvar(any(), anyList());
//
//        mockMvc.perform(multipart("/produto")
//                        .file(produtoPart)
//                        .file(imagemPart)
//                        .contentType(MediaType.MULTIPART_FORM_DATA))
//                .andExpect(status().isBadRequest())
//                .andExpect(content().string(org.hamcrest.Matchers.containsString("Não foi possível criar anúncio")));
//    }

//    @Test
//    void atualizarProduto_deveRetornarOk_quandoSucesso() throws Exception {
//        Produto produto = new Produto();
//        produto.setNome("Produto Atualizado");
//
//        MockMultipartFile produtoPart = new MockMultipartFile("produto", "", "application/json",
//                objectMapper.writeValueAsBytes(produto));
//        MockMultipartFile imagemPart = new MockMultipartFile("imagens", "imagem.jpg", "image/jpeg",
//                "imagem".getBytes());
//
//        mockMvc.perform(multipart("/produto/1")
//                        .file(produtoPart)
//                        .file(imagemPart)
//                        .contentType(MediaType.MULTIPART_FORM_DATA)
//                        .with(request -> {
//                            request.setMethod("PUT");
//                            return request;
//                        }))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Produto atualizado com sucesso"));
//    }

    @Test
    void atualizarStatus_deveRetornarOk_quandoStatusAtivo() throws Exception {
        mockMvc.perform(put("/produto/1/status")
                        .param("status", StatusProduto.ATIVO.name()))
                .andExpect(status().isOk())
                .andExpect(content().string("Anúncio ativado com sucesso"));
    }

//    @Test
//    void atualizarStatus_deveRetornarBadRequest_quandoLudiBoxException() throws Exception {
//        doThrow(new LudiBoxException("Erro", "Não foi possível atualizar", null))
//                .when(produtoService).atualizarStatus(anyInt(), any());
//
//        mockMvc.perform(put("/produto/1/status")
//                        .param("status", StatusProduto.INATIVO.name()))
//                .andExpect(status().isBadRequest())
//                .andExpect(content().string(org.hamcrest.Matchers.containsString("Não foi possível inativar anúncio")));
//    }

    @Test
    void atualizarBloqueio_deveRetornarOk_quandoSucesso() throws Exception {
        mockMvc.perform(put("/produto/bloqueio/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Bloqueio atualizado com sucesso"));
    }

    @Test
    void deletarProduto_deveRetornarOk_quandoSucesso() throws Exception {
        mockMvc.perform(delete("/produto/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Anúncio excluído com sucesso"));
    }

    @Test
    void listarTodos_deveRetornarLista() throws Exception {
        ProdutoListarDto dto = new ProdutoListarDto();
        dto.setId(1);
        List<ProdutoListarDto> lista = List.of(dto);

        when(produtoService.buscarTodos()).thenReturn(lista);

        mockMvc.perform(get("/produto/listar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dto.getId()));
    }

    @Test
    void listarTodosComFiltro_deveRetornarPagina() throws Exception {
        ProdutoListarDto dto = new ProdutoListarDto();
        dto.setId(1);

        Page<ProdutoListarDto> page = new PageImpl<>(List.of(dto));
        when(produtoService.buscarComFiltro(anyString(), anyString(), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/produto/listarComFiltro")
                        .param("nome", "teste")
                        .param("page", "0")
                        .param("size", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(dto.getId()));
    }

    @Test
    void buscarProduto_deveRetornarDetalhes() throws Exception {
        ProdutoDetalheDto detalheDto = new ProdutoDetalheDto();
        detalheDto.setId(1);

        when(produtoService.buscar(1)).thenReturn(detalheDto);

        mockMvc.perform(get("/produto/buscar/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(detalheDto.getId()));
    }

    @Test
    void listarPorUsuario_deveRetornarLista() throws Exception {
        Produto produto = new Produto();
        produto.setId(1);

        when(produtoService.listarPorUsuario(1)).thenReturn(List.of(produto));

        mockMvc.perform(get("/produto/pessoa/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(produto.getId()));
    }
}

package br.com.ludibox.controller;

import br.com.ludibox.model.entity.Locacao;
import br.com.ludibox.model.entity.ProdutoLocacao;
import br.com.ludibox.service.LocacaoService;
import br.com.ludibox.service.PessoaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(LocacaoController.class)
class LocacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocacaoService locacaoService;

    @MockBean
    private PessoaService pessoaService;

    @Autowired
    private ObjectMapper objectMapper;

    private Locacao locacao;
    private ProdutoLocacao produtoLocacao;

    @BeforeEach
    void setup() {
        locacao = new Locacao();
        locacao.setId(1);
        // configure outros campos se desejar

        produtoLocacao = new ProdutoLocacao();
        produtoLocacao.setId(1);
        // configure outros campos se desejar
    }

    @Test
    void abrirNovaLocacao_deveCriarLocacaoERetornarCreated() throws Exception {
        when(locacaoService.abrirNovaLocacao(any(Locacao.class))).thenReturn(locacao);

        mockMvc.perform(post("/locacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locacao)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(locacao.getId()));
    }

//    @Test
//    void incluirProdutoNaLocacao_deveRetornarLocacaoAtualizada() throws Exception {
//        when(locacaoService.incluirProdutoNaLocacao(eq(1), any(ProdutoLocacao.class))).thenReturn(locacao);
//
//        mockMvc.perform(post("/locacao/{id}/produtos", 1)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(produtoLocacao)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(locacao.getId()));
//    }

    @Test
    void retirarProdutoDaLocacao_deveRetornarLocacaoAtualizada() throws Exception {
        when(locacaoService.retirarProdutoDaLocacao(1, 1)).thenReturn(locacao);

        mockMvc.perform(delete("/locacao/{id}/produtos/{produtoLocacaoId}", 1, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(locacao.getId()));
    }

    @Test
    void deletarLocacao_deveRetornarNoContent() throws Exception {
        doNothing().when(locacaoService).deletarLocacao(1);

        mockMvc.perform(delete("/locacao/{id}", 1))
                .andExpect(status().isNoContent());

        verify(locacaoService).deletarLocacao(1);
    }

    @Test
    void cancelarLocacao_deveRetornarOkQuandoSucesso() throws Exception {
        doNothing().when(locacaoService).cancelarLocacao(1, "motivo qualquer");

        mockMvc.perform(post("/locacao/{id}/cancelar", 1)
                        .param("motivoCancelamento", "motivo qualquer"))
                .andExpect(status().isOk())
                .andExpect(content().string("Locação cancelada com sucesso."));
    }

    @Test
    void cancelarLocacao_deveRetornarBadRequestQuandoFalha() throws Exception {
        doThrow(new RuntimeException("Erro de cancelamento")).when(locacaoService).cancelarLocacao(1, "motivo");

        mockMvc.perform(post("/locacao/{id}/cancelar", 1)
                        .param("motivoCancelamento", "motivo"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Erro de cancelamento"));
    }

    @Test
    void escolherEnderecoEntrega_deveRetornarOk() throws Exception {
        doNothing().when(locacaoService).escolherEnderecoEntrega(1, 2, 3);

        mockMvc.perform(put("/locacao/{locacaoId}/endereco-entrega/{enderecoId}", 1, 2)
                        .param("locadorId", "3"))
                .andExpect(status().isOk());

        verify(locacaoService).escolherEnderecoEntrega(1, 2, 3);
    }

    @Test
    void buscarLocacaoPendente_deveRetornarLocacaoQuandoEncontrada() throws Exception {
        when(locacaoService.buscarLocacaoPendentePorUsuarioId(1)).thenReturn(Optional.of(locacao));

        mockMvc.perform(get("/locacao/pendente/{usuarioId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(locacao.getId()));
    }

//    @Test
//    void buscarLocacaoPendente_deveRetornarOkComNullQuandoNaoEncontrada() throws Exception {
//        when(locacaoService.buscarLocacaoPendentePorUsuarioId(1)).thenReturn(Optional.empty());
//
//        mockMvc.perform(get("/locacao/pendente/{usuarioId}", 1))
//                .andExpect(status().isOk())
//                .andExpect(content().string("null"));
//    }

    @Test
    void buscarPorId_deveRetornarLocacao() throws Exception {
        when(locacaoService.buscarPorId(1)).thenReturn(locacao);

        mockMvc.perform(get("/locacao/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(locacao.getId()));
    }

    @Test
    void finalizarLocacao_deveRetornarOk() throws Exception {
        doNothing().when(locacaoService).finalizarLocacao(1, 2, 3);

        mockMvc.perform(post("/locacao/finalizar/{locacaoId}/{enderecoId}/{locadorId}", 1, 2, 3))
                .andExpect(status().isOk());

        verify(locacaoService).finalizarLocacao(1, 2, 3);
    }

//    @Test
//    void listarLocacoesRecebidas_deveRetornarLista() throws Exception {
//        when(locacaoService.obterLocacoesRecebidas(1)).thenReturn(List.of(produtoLocacao));
//
//        mockMvc.perform(get("/locacao/recebidas/{usuarioId}", 1))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].id").value(produtoLocacao.getId()));
//    }
//
//    @Test
//    void listarLocacoesEfetuadas_deveRetornarLista() throws Exception {
//        when(locacaoService.obterLocacoesEfetuadas()).thenReturn(List.of(produtoLocacao));
//
//        mockMvc.perform(get("/locacao/efetuadas/{usuarioId}", 1))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].id").value(produtoLocacao.getId()));
//    }
//
//    @Test
//    void atualizarStatus_deveChamarServicoERetornarOk() throws Exception {
//        doNothing().when(locacaoService).atualizarStatus(1, "statusQualquer");
//
//        mockMvc.perform(put("/locacao/status/{locacaoId}", 1)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("\"statusQualquer\""))
//                .andExpect(status().isOk());
//
//        verify(locacaoService).atualizarStatus(1, "statusQualquer");
//    }

    @Test
    void listarTodasAsLocacoes_deveRetornarLista() throws Exception {
        when(locacaoService.obterTodasAsLocacoes()).thenReturn(List.of(locacao));

        mockMvc.perform(get("/locacao/listarTodasLocacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(locacao.getId()));
    }

    @Test
    void filtrarTodasLocacoes_deveRetornarLista() throws Exception {
        when(locacaoService.filtrarLocacoes(any(), any(), any(), any())).thenReturn(List.of(locacao));

        mockMvc.perform(get("/locacao/filtrarTodasLocacoes")
                        .param("dataInicio", "2023-01-01")
                        .param("dataFim", "2023-12-31")
                        .param("valorMin", "10.5")
                        .param("valorMax", "100.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(locacao.getId()));
    }
}

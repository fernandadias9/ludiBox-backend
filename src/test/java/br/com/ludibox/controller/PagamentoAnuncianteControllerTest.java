package br.com.ludibox.controller;

import br.com.ludibox.model.entity.PagamentosAnunciante;
import br.com.ludibox.service.PagamentosAnuncianteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(PagamentosAnuncianteController.class)
class PagamentosAnuncianteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PagamentosAnuncianteService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void salvar_deveRetornarPagamentoSalvo() throws Exception {
        PagamentosAnunciante pagamento = new PagamentosAnunciante();
        pagamento.setId(1);
        pagamento.setNomeAnunciante("teste");

        when(service.salvar(any(PagamentosAnunciante.class))).thenReturn(pagamento);

        mockMvc.perform(post("/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pagamento)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pagamento.getId()))
                .andExpect(jsonPath("$.nomeAnunciante").value(pagamento.getNomeAnunciante()));
    }
}

package br.com.ludibox.controller;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.dto.CepDTO;
import br.com.ludibox.model.entity.Endereco;
import br.com.ludibox.service.CepService;
import br.com.ludibox.service.EnderecoService;
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
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(EnderecoController.class)
class EnderecoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnderecoService enderecoService;

    @MockBean
    private CepService cepService;

    private Endereco endereco;
    private CepDTO cepDTO;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        endereco = new Endereco();
        endereco.setId(1);
        endereco.setRua("Rua Teste");
        endereco.setNumero(123);
        // Preencha mais campos se necessário

        cepDTO = new CepDTO();
        cepDTO.setCep("12345678");
        cepDTO.setLogradouro("Rua Cep Teste");
        cepDTO.setLocalidade("Cidade Teste");
        cepDTO.setEstado("Estado Teste");
    }

    @Test
    void salvarEnderecoParaPessoa_deveRetornarEnderecoSalvo() throws Exception {
        when(enderecoService.salvarEnderecoParaPessoa(any(Endereco.class))).thenReturn(endereco);

        mockMvc.perform(post("/endereco/novo-endereco")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(endereco)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(endereco.getId()))
                .andExpect(jsonPath("$.rua").value(endereco.getRua()));
    }

    @Test
    void atualizarEnderecoParaPessoa_deveAtualizarEVoltarEndereco() throws Exception {
        Map<String, Object> updates = Map.of("rua", "Rua Atualizada");
        when(enderecoService.buscarPorId(1)).thenReturn(endereco);
        when(enderecoService.atualizarEnderecoPessoa(eq(endereco), anyMap())).thenReturn(endereco);

        mockMvc.perform(patch("/endereco/atualizar-endereco/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(endereco.getId()));

        verify(enderecoService).atualizarEnderecoPessoa(eq(endereco), anyMap());
    }


    @Test
    void atualizarEnderecoParaPessoa_deveRetornarNotFoundSeEnderecoNaoExistir() throws Exception {
        when(enderecoService.buscarPorId(1)).thenReturn(null);

        mockMvc.perform(patch("/endereco/atualizar-endereco/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());

        verify(enderecoService, never()).atualizarEnderecoPessoa(any(), anyMap());
    }

    @Test
    void buscarEnderecoPorCep_deveRetornarCepDTO() throws Exception {
        when(cepService.buscarEnderecoPorCep(12345678)).thenReturn(cepDTO);

        mockMvc.perform(get("/endereco/buscar_por_cep/{cep}", 12345678))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value(cepDTO.getCep()))
                .andExpect(jsonPath("$.logradouro").value(cepDTO.getLogradouro()));
    }

    @Test
    void buscarEnderecosPorPessoa_deveRetornarLista() throws Exception {
        when(enderecoService.listarEnderecosPorPessoa(1)).thenReturn(List.of(endereco));

        mockMvc.perform(get("/endereco/pessoa/{pessoaId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(endereco.getId()));
    }

    @Test
    void deletarEndereco_deveRetornarNoContent() throws Exception {
        doNothing().when(enderecoService).deletar(1);

        mockMvc.perform(delete("/endereco/deletar-endereco/{id}", 1))
                .andExpect(status().isNoContent());

        verify(enderecoService).deletar(1);
    }

//    @Test
//    void buscarPorId_deveRetornarEndereco() throws Exception {
//        Endereco endereco = new Endereco();
//        endereco.setId(1);
//        endereco.setRua("Rua Teste");
//
//        when(enderecoService.buscarPorId(1)).thenReturn(endereco);
//
//        mockMvc.perform(get("/endereco/{id}", 1))
//                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.id").value(1))
//                .andExpect(jsonPath("$.rua").value("Rua Teste"));
//    }

}

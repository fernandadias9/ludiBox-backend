package br.com.ludibox.controller;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.dto.SenhasDTO;
import br.com.ludibox.service.ResetarSenhaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(RecuperacaoSenhaController.class)
public class RecuperacaoSenhaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResetarSenhaService resetarSenhaService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void resetarSenha_deveRetornarOk_quandoEmailExiste() throws Exception {
        Map<String, String> request = Map.of("email", "teste@email.com");

        mockMvc.perform(post("/api/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Sua nova senha foi enviada para seu e-mail."));
    }

    @Test
    void resetarSenha_deveRetornarNotFound_quandoEmailNaoExiste() throws Exception {
        Map<String, String> request = Map.of("email", "invalido@email.com");

        doThrow(new UsernameNotFoundException("Email não encontrado"))
                .when(resetarSenhaService).resetarSenha(anyString());

        mockMvc.perform(post("/api/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("E-mail não encontrado."));
    }

    @Test
    void editarSenha_deveRetornarOk_quandoSucesso() throws Exception {
        SenhasDTO senhasDTO = new SenhasDTO();
        senhasDTO.setSenhaAtual("senhaAntiga");
        senhasDTO.setNovaSenha("novaSenha123");
        senhasDTO.setConfirmarSenha("novaSenha123");

        mockMvc.perform(put("/api/password/editar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(senhasDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void editarSenha_deveRetornarBadRequest_quandoLudiBoxException() throws Exception {
        SenhasDTO senhasDTO = new SenhasDTO();
        senhasDTO.setSenhaAtual("senhaAntiga");
        senhasDTO.setNovaSenha("novaSenha123");
        senhasDTO.setConfirmarSenha("novaSenha123");

        doThrow(new LudiBoxException("Erro", "Senha incorreta", null))
                .when(resetarSenhaService).alterarSenha(any());

        mockMvc.perform(put("/api/password/editar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(senhasDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Senha incorreta"));
    }

    @Test
    void editarSenha_deveRetornarInternalServerError_quandoExceptionGenerica() throws Exception {
        SenhasDTO senhasDTO = new SenhasDTO();
        senhasDTO.setSenhaAtual("senhaAntiga");
        senhasDTO.setNovaSenha("novaSenha123");
        senhasDTO.setConfirmarSenha("novaSenha123");

        doThrow(new RuntimeException("Erro inesperado"))
                .when(resetarSenhaService).alterarSenha(any());

        mockMvc.perform(put("/api/password/editar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(senhasDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Erro inesperado ao alterar a senha"));
    }
}

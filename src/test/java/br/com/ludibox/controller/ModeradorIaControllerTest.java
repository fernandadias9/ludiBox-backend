//package br.com.ludibox.controller;
//
//import br.com.ludibox.exception.LudiBoxException;
//import br.com.ludibox.model.entity.Produto;
//import br.com.ludibox.service.IA.ValidadorConteudoService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Map;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@AutoConfigureMockMvc(addFilters = false)
//@WebMvcTest(ModeradorIaController.class)
//class ModeradorIaControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private ValidadorConteudoService validadorConteudoService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    void testarIA_deveRetornarProdutoValido_quandoServicoValidaSucesso() throws Exception {
//        Produto produtoMock = new Produto();
//        produtoMock.setId(1);
//        produtoMock.setNome("Produto Teste");
//
//        when(validadorConteudoService.validar(any(Produto.class))).thenReturn(produtoMock);
//
//        mockMvc.perform(post("/public/ia/testar")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(produtoMock)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(produtoMock.getId()))
//                .andExpect(jsonPath("$.nome").value(produtoMock.getNome()));
//    }
//
//    @Test
//    void testarIA_deveRetornarErroCustomizado_quandoServicoLancaLudiBoxException() throws Exception {
//        Produto produtoRequest = new Produto();
//        produtoRequest.setNome("Produto Inválido");
//
//        LudiBoxException ludiBoxException = new LudiBoxException("campo", "mensagem de erro", HttpStatus.BAD_REQUEST);
//
//        when(validadorConteudoService.validar(any(Produto.class))).thenThrow(ludiBoxException);
//
//        mockMvc.perform(post("/public/ia/testar")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(produtoRequest)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.erro").value("campo"))
//                .andExpect(jsonPath("$.detalhes").value("mensagem de erro"));
//    }
//
//    @Test
//    void testarIA_deveRetornarErroInterno_quandoServicoLancaExcecaoGenerica() throws Exception {
//        Produto produtoRequest = new Produto();
//        produtoRequest.setNome("Produto Teste");
//
//        when(validadorConteudoService.validar(any(Produto.class))).thenThrow(new RuntimeException("erro inesperado"));
//
//        mockMvc.perform(post("/public/ia/testar")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(produtoRequest)))
//                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$.erro").value("Erro interno"))
//                .andExpect(jsonPath("$.detalhes").value("erro inesperado"));
//    }
//}

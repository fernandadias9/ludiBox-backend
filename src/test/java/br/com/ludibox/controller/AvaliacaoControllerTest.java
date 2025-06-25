//package br.com.ludibox.controller;
//
//import br.com.ludibox.auth.AuthenticationService;
//import br.com.ludibox.model.dto.AvaliacaoRequestDTO;
//import br.com.ludibox.model.entity.Avaliacao;
//import br.com.ludibox.model.entity.Pessoa;
//import br.com.ludibox.service.AvaliacaoService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@AutoConfigureMockMvc(addFilters = false)
//@WebMvcTest(AvaliacaoController.class)
//class AvaliacaoControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private AvaliacaoService avaliacaoService;
//
//    @MockBean
//    private AuthenticationService authService;
//
//    private Pessoa pessoa;
//    private Avaliacao avaliacao;
//
//    @BeforeEach
//    void setup() {
//        pessoa = new Pessoa();
//        pessoa.setId(1);
//        pessoa.setNome("Usuário Teste");
//
//        avaliacao = new Avaliacao();
//        avaliacao.setId(10);
//        avaliacao.setEstrelas(5);
//        avaliacao.setComentario("Muito bom!");
//    }
//
//    @Test
//    void deveSalvarAvaliacao() throws Exception {
//        AvaliacaoRequestDTO dto = new AvaliacaoRequestDTO();
//        dto.setProdutoLocacaoId(100);
//        dto.setEstrelas(5);
//        dto.setComentario("Excelente!");
//
//        Mockito.when(authService.getPessoaAutenticada()).thenReturn(pessoa);
//        Mockito.when(avaliacaoService.salvar((int) anyLong(), (int) anyLong(), anyInt(), anyString()))
//                .thenReturn(avaliacao);
//
//        mockMvc.perform(post("/avaliacoes")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(new ObjectMapper().writeValueAsString(dto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(10L))
//                .andExpect(jsonPath("$.estrelas").value(5))
//                .andExpect(jsonPath("$.comentario").value("Muito bom!"));
//    }
//
//    @Test
//    void deveAlterarAvaliacao() throws Exception {
//        Mockito.when(avaliacaoService.alterar(eq(10L), eq(4), eq(1L)))
//                .thenReturn(avaliacao);
//
//        mockMvc.perform(put("/avaliacoes/10")
//                        .param("estrelas", "4")
//                        .principal(() -> "1")) // Simula o AuthenticationPrincipal
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(10L));
//    }
//
//    @Test
//    void deveDeletarAvaliacao() throws Exception {
//        mockMvc.perform(delete("/avaliacoes/10")
//                        .principal(() -> "1"))
//                .andExpect(status().isNoContent());
//
//        Mockito.verify(avaliacaoService).deletar(10L, 1L);
//    }
//}

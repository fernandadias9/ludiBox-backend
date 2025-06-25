package br.com.ludibox.controller;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.dto.PerfilDTO;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.service.PessoaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(PessoaController.class)
class PessoaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PessoaService pessoaService;

    @MockBean
    private AuthenticationService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void uploadPessoa_deveRetornarOk_quandoImagemValidaEUsuarioAutenticado() throws Exception {
        MockMultipartFile imagem = new MockMultipartFile("imagem", "foto.jpg",
                MediaType.IMAGE_JPEG_VALUE, "imagemConteudo".getBytes());

        Pessoa pessoaMock = new Pessoa();
        pessoaMock.setId(1);

        when(authService.getPessoaAutenticada()).thenReturn(pessoaMock);
        doNothing().when(pessoaService).salvarImagemPessoa(any(), anyInt());

        mockMvc.perform(multipart("/pessoa/upload/1")
                        .file(imagem)
                        .with(request -> {
                            request.setMethod("PATCH"); // para multipart com PATCH
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(content().string("Imagem atualizada com sucesso"));
    }

    @Test
    void uploadPessoa_deveRetornarBadRequest_quandoImagemNula() throws Exception {
        mockMvc.perform(multipart("/pessoa/upload/1")
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadPessoa_deveRetornarUnauthorized_quandoUsuarioNaoAutenticado() throws Exception {
        MockMultipartFile imagem = new MockMultipartFile("imagem", "foto.jpg",
                MediaType.IMAGE_JPEG_VALUE, "imagemConteudo".getBytes());

        when(authService.getPessoaAutenticada()).thenReturn(null);

        mockMvc.perform(multipart("/pessoa/upload/1")
                        .file(imagem)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isUnauthorized());
    }

//    @Test
//    void cadastrarAdm_deveRetornarPessoaSalva() throws Exception {
//        Pessoa pessoa = new Pessoa();
//        pessoa.setId(1);
//        pessoa.setNome("Admin");
//
//        when(pessoaService.cadastrarAdm(any(Pessoa.class))).thenReturn(pessoa);
//
//        mockMvc.perform(post("/pessoa/cadastrar_adm")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(pessoa)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(pessoa.getId()))
//                .andExpect(jsonPath("$.nome").value(pessoa.getNome()));
//    }

//    @Test
//    void atualizarPessoa_deveRetornarOk_quandoPessoaExisteESemErroValidacao() throws Exception {
//        Pessoa pessoaMock = new Pessoa();
//        pessoaMock.setId(1);
//        pessoaMock.setNome("João");
//
//        when(pessoaService.buscarPorId(1)).thenReturn(pessoaMock);
//        doNothing().when(pessoaService).atualizarDados(any(), anyMap());
//        BindingResult bindingResultMock = mock(BindingResult.class);
//        when(bindingResultMock.hasErrors()).thenReturn(false);
//
//        mockMvc.perform(patch("/pessoa/atualizar/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"nome\":\"João Updated\"}"))
//                .andExpect(status().isOk());
//    }

    @Test
    void atualizarPessoa_deveRetornarNotFound_quandoPessoaNaoExiste() throws Exception {
        when(pessoaService.buscarPorId(1)).thenReturn(null);

        mockMvc.perform(patch("/pessoa/atualizar/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

//    @Test
//    void buscarTodasPessoas_deveRetornarLista() throws Exception {
//        Pessoa p1 = new Pessoa();
//        p1.setId(1);
//        Pessoa p2 = new Pessoa();
//        p2.setId(2);
//
//        when(pessoaService.buscarTodos()).thenReturn(Arrays.asList(p1, p2));
//
//        mockMvc.perform(get("/pessoa"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].id").value(p1.getId()))
//                .andExpect(jsonPath("$[1].id").value(p2.getId()));
//    }

    @Test
    void buscarPerfilPorId_deveRetornarPerfil() throws Exception {
        PerfilDTO perfilDTO = new PerfilDTO();
        perfilDTO.setId(1);
        perfilDTO.setNome("Perfil Teste");

        when(pessoaService.buscarPerfilPorId(1)).thenReturn(perfilDTO);

        mockMvc.perform(get("/pessoa/buscar_perfil/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(perfilDTO.getId()))
                .andExpect(jsonPath("$.nome").value(perfilDTO.getNome()));
    }
}

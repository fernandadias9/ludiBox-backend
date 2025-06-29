package br.com.ludibox.controller;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.model.dto.AuthResponseDTO;
import br.com.ludibox.model.dto.LoginRequestDTO;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.enums.EnumPerfil;
import br.com.ludibox.service.PessoaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private PessoaService pessoaService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAuthenticatePessoa_Success() throws Exception {
        LoginRequestDTO requestDTO = new LoginRequestDTO();
        requestDTO.setUsername("user");
        requestDTO.setPassword("pass");

        Authentication authentication = new UsernamePasswordAuthenticationToken("user", "pass");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authenticationService.authenticatePessoa(authentication)).thenReturn("jwt-token");

        mockMvc.perform(post("/auth/authenticatePessoa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

//    @Test
//    void testCadastrarAdm_Success() throws Exception {
//        Pessoa pessoa = new Pessoa();
//        pessoa.setId(1);
//        pessoa.setEmail("admin@email.com");
//        pessoa.setPerfil(EnumPerfil.ADMINISTRADOR);
//
//        when(pessoaService.cadastrarAdm(any(Pessoa.class))).thenReturn(pessoa);
//
//        mockMvc.perform(post("/auth/cadastrar_adm")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(pessoa)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(1L))
//                .andExpect(jsonPath("$.email").value("admin@email.com"));
//    }
//
//    @Test
//    void testRegistrarPessoa_Success() throws Exception {
//        Pessoa novaPessoa = new Pessoa();
//        novaPessoa.setEmail("pessoa@email.com");
//        novaPessoa.setPerfil(EnumPerfil.USUARIO);
//
//        when(pessoaService.salvar(any(Pessoa.class))).thenReturn(novaPessoa);
//
//        mockMvc.perform(post("/auth/nova-pessoa")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(novaPessoa)))
//                .andExpect(status().isCreated());
//    }

}

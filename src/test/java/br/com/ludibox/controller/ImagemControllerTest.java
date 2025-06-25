package br.com.ludibox.controller;

import br.com.ludibox.service.ImagemService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ImagemController.class)
class ImagemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImagemService imagemService;

    @Test
    void uploadImagem_deveRetornarBase64_quandoSucesso() throws Exception {
        String base64Fake = "aGVsbG8gd29ybGQ="; // "hello world" em base64

        when(imagemService.processarImagem(any())).thenReturn(base64Fake);

        MockMultipartFile arquivo = new MockMultipartFile(
                "file",
                "teste.png",
                MediaType.IMAGE_PNG_VALUE,
                "conteudo da imagem".getBytes()
        );

        mockMvc.perform(multipart("/api/imagem/upload")
                        .file(arquivo))
                .andExpect(status().isOk())
                .andExpect(content().string(base64Fake));
    }

    @Test
    void uploadImagem_deveRetornarErro500_quandoServicoLancarExcecao() throws Exception {
        when(imagemService.processarImagem(any())).thenThrow(new RuntimeException("Erro inesperado"));

        MockMultipartFile arquivo = new MockMultipartFile(
                "file",
                "teste.png",
                MediaType.IMAGE_PNG_VALUE,
                "conteudo da imagem".getBytes()
        );

        mockMvc.perform(multipart("/api/imagem/upload")
                        .file(arquivo))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Erro ao processar imagem: Erro inesperado"));
    }
}

package br.com.ludibox.service;

import br.com.ludibox.exception.LudiBoxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImagemServiceTest {

    private ImagemService imagemService;

    @BeforeEach
    void setUp() {
        imagemService = new ImagemService();
    }

    @Test
    void testProcessarImagem_ComSucesso() throws Exception {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        byte[] imagemBytes = "imagem_teste".getBytes(StandardCharsets.UTF_8);
        when(mockFile.getBytes()).thenReturn(imagemBytes);

        // Act
        String base64 = imagemService.processarImagem(mockFile);

        // Assert
        String esperado = Base64.getEncoder().encodeToString(imagemBytes);
        assertEquals(esperado, base64);
    }

    @Test
    void testProcessarImagem_ComIOException() throws Exception {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getBytes()).thenThrow(new IOException("Erro de leitura"));

        // Act & Assert
        LudiBoxException ex = assertThrows(LudiBoxException.class, () -> imagemService.processarImagem(mockFile));
        assertEquals("Erro: ", ex.getCampo());
        assertEquals("Erro ao processar arquivo", ex.getMensagem());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getHttpStatus());
    }
}

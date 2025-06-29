package br.com.ludibox.service.IA;

import br.com.ludibox.exception.LudiBoxException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModeradorIAClientTest {

    @InjectMocks
    private ModeradorIAClient moderadorIAClient;

    @Mock
    private RestTemplate restTemplate;

    private String geminiApiKey = "test-api-key";
    private String prompt = "Teste de prompt";
    private String origem = "Teste";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(moderadorIAClient, "geminiApiKey", geminiApiKey);
    }

    @Test
    void testEnviarPromptEValidar_SemViolacao_ComSucesso() {
        String responseBody = """
            {
                "candidates": [{
                    "content": {
                        "parts": [{
                            "text": "```json\\n{\\"violation\\": false}\\n```"
                        }]
                    }
                }]
            }
            """;

        ResponseEntity<String> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        assertDoesNotThrow(() -> moderadorIAClient.enviarPromptEValidar(prompt, origem));

        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void testEnviarPromptEValidar_ComViolacao_DeveLancarExcecao() {
        String responseBody = """
            {
                "candidates": [{
                    "content": {
                        "parts": [{
                            "text": "```json\\n{\\"violation\\": true, \\"message\\": \\"Conteúdo inadequado\\"}\\n```"
                        }]
                    }
                }]
            }
            """;

        ResponseEntity<String> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> moderadorIAClient.enviarPromptEValidar(prompt, origem));

        assertEquals(origem, exception.getCampo());
        assertEquals("Conteúdo inadequado", exception.getMensagem());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getHttpStatus());
    }

    @Test
    void testEnviarPromptEValidar_ComViolacaoSemMensagem_DeveLancarExcecaoComMensagemPadrao() {
        String responseBody = """
            {
                "candidates": [{
                    "content": {
                        "parts": [{
                            "text": "```json\\n{\\"violation\\": true}\\n```"
                        }]
                    }
                }]
            }
            """;

        ResponseEntity<String> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> moderadorIAClient.enviarPromptEValidar(prompt, origem));

        assertEquals(origem, exception.getCampo());
        assertEquals("Violação detectada", exception.getMensagem());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getHttpStatus());
    }

    @Test
    void testEnviarPromptEValidar_JsonSemChaves_ComSucesso() {
        String responseBody = """
            {
                "candidates": [{
                    "content": {
                        "parts": [{
                            "text": "{\\"other\\": \\"value\\"}"
                        }]
                    }
                }]
            }
            """;

        ResponseEntity<String> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        assertDoesNotThrow(() -> moderadorIAClient.enviarPromptEValidar(prompt, origem));
    }

    @Test
    void testEnviarPromptEValidar_JsonInvalido_DeveLancarExcecao() {
        String responseBody = """
            {
                "candidates": [{
                    "content": {
                        "parts": [{
                            "text": "json inválido {"
                        }]
                    }
                }]
            }
            """;

        ResponseEntity<String> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> moderadorIAClient.enviarPromptEValidar(prompt, origem));

        assertEquals(origem, exception.getCampo());
        assertEquals("Erro interno ao validar com IA", exception.getMensagem());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }

    @Test
    void testEnviarPromptEValidar_ErroRestTemplate_DeveLancarExcecao() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException("Erro de conexão"));

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> moderadorIAClient.enviarPromptEValidar(prompt, origem));

        assertEquals(origem, exception.getCampo());
        assertEquals("Erro interno ao validar com IA", exception.getMensagem());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }

    @Test
    void testEnviarPromptEValidar_ResponseBodyNulo_DeveLancarExcecao() {
        ResponseEntity<String> response = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> moderadorIAClient.enviarPromptEValidar(prompt, origem));

        assertEquals(origem, exception.getCampo());
        assertEquals("Erro interno ao validar com IA", exception.getMensagem());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }

    @Test
    void testEnviarPromptEValidar_TextoSemJson_DeveLancarExcecao() {
        String responseBody = """
            {
                "candidates": [{
                    "content": {
                        "parts": [{
                            "text": "Texto sem JSON válido"
                        }]
                    }
                }]
            }
            """;

        ResponseEntity<String> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        LudiBoxException exception = assertThrows(LudiBoxException.class,
                () -> moderadorIAClient.enviarPromptEValidar(prompt, origem));

        assertEquals(origem, exception.getCampo());
        assertEquals("Erro interno ao validar com IA", exception.getMensagem());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }

    @Test
    void testEnviarPromptEValidar_ViolacaoFalse_ComSucesso() {
        String responseBody = """
            {
                "candidates": [{
                    "content": {
                        "parts": [{
                            "text": "{\\"violation\\": false, \\"message\\": \\"Conteúdo aprovado\\"}"
                        }]
                    }
                }]
            }
            """;

        ResponseEntity<String> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        assertDoesNotThrow(() -> moderadorIAClient.enviarPromptEValidar(prompt, origem));
    }

    @Test
    void testEnviarPromptEValidar_VerificarCorpoRequisicao() {
        String responseBody = """
            {
                "candidates": [{
                    "content": {
                        "parts": [{
                            "text": "{\\"violation\\": false}"
                        }]
                    }
                }]
            }
            """;

        ResponseEntity<String> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        moderadorIAClient.enviarPromptEValidar(prompt, origem);

        verify(restTemplate).postForEntity(
                eq("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey),
                argThat(entity -> {
                    HttpEntity<Map<String, Object>> httpEntity = (HttpEntity<Map<String, Object>>) entity;
                    Map<String, Object> body = httpEntity.getBody();

                    assertNotNull(body);
                    assertTrue(body.containsKey("contents"));

                    List<Map<String, Object>> contents = (List<Map<String, Object>>) body.get("contents");
                    assertEquals(1, contents.size());

                    Map<String, Object> content = contents.get(0);
                    assertEquals("user", content.get("role"));

                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    assertEquals(1, parts.size());
                    assertEquals(prompt, parts.get(0).get("text"));

                    HttpHeaders headers = httpEntity.getHeaders();
                    assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());

                    return true;
                }),
                eq(String.class)
        );
    }

    @Test
    void testEnviarPromptEValidar_ComPromptVazio_ComSucesso() {
        String emptyPrompt = "";
        String responseBody = """
            {
                "candidates": [{
                    "content": {
                        "parts": [{
                            "text": "{\\"violation\\": false}"
                        }]
                    }
                }]
            }
            """;

        ResponseEntity<String> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        assertDoesNotThrow(() -> moderadorIAClient.enviarPromptEValidar(emptyPrompt, origem));
    }

    @Test
    void testEnviarPromptEValidar_ComOrigemVazia_ComSucesso() {
        String origemVazia = "";
        String responseBody = """
            {
                "candidates": [{
                    "content": {
                        "parts": [{
                            "text": "{\\"violation\\": false}"
                        }]
                    }
                }]
            }
            """;

        ResponseEntity<String> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        assertDoesNotThrow(() -> moderadorIAClient.enviarPromptEValidar(prompt, origemVazia));
    }
}
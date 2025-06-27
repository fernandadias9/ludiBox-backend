package br.com.ludibox.service.IA;

import br.com.ludibox.exception.LudiBoxException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ModeradorIAClient {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Autowired
    private RestTemplate restTemplate;

    public void enviarPromptEValidar(String prompt, String origem) throws LudiBoxException {
        try {
            Map<String, Object> requestBody = criarCorpoRequisicao(prompt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(getUrlAi(), request, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");

            String texto = limparTexto(textNode.asText());

            JsonNode json = mapper.readTree(texto);
            if (json.has("violation") && json.get("violation").asBoolean()) {
                String mensagem = json.has("message") ? json.get("message").asText() : "Violação detectada";
                throw new LudiBoxException(origem, mensagem, HttpStatus.UNPROCESSABLE_ENTITY);
            }

        } catch (LudiBoxException e) {
            throw e;
        } catch (Exception e) {
            throw new LudiBoxException(origem, "Erro interno ao validar com IA", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getUrlAi() {
        return "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey;
    }

    private Map<String, Object> criarCorpoRequisicao(String prompt) {
        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> user = Map.of("role", "user", "parts", List.of(part));
        return Map.of("contents", List.of(user));
    }

    private String limparTexto(String texto) {
        int inicio = texto.indexOf("{");
        int fim = texto.lastIndexOf("}");
        if (inicio != -1 && fim != -1 && inicio < fim) {
            return texto.substring(inicio, fim + 1).trim();
        }
        return texto.trim();
    }
}

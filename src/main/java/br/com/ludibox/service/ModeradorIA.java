package br.com.ludibox.service;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.entity.Produto;
import br.com.ludibox.model.repository.ProdutoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class ModeradorIA {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ModeradorIA.class);
    private final ProdutoRepository produtoRepository;
    private final AuthenticationService authService;
    private final ImagemService imagemService;
    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public String getUrlAi() {
        return "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey;
    }

    @Autowired
    public ModeradorIA(
            ProdutoRepository produtoRepository, AuthenticationService authService,
            ImagemService imagemService, RestTemplate restTemplate) {
                this.produtoRepository = produtoRepository;
                this.authService = authService;
                this.imagemService = imagemService;
                this.restTemplate = restTemplate;
    }

    public Produto validarConteudoProduto(Produto produto) throws LudiBoxException {
        String prompt = String.format(
                "Analise o seguinte nome e descrição do produto. Verifique se contém linguagem obscena, ofensiva ou qualquer conteúdo impróprio.\n" +
                        "Se o nome OU a descrição contiverem conteúdo impróprio, retorne um JSON com a seguinte estrutura: {\"violation\": true, \"message\": \"Conteúdo impróprio detectado.\"}.\n" +
                        "Caso contrário, retorne {\"violation\": false}.\n" +
                        "Produto: {\"nome\": \"%s\", \"descricao\": \"%s\"}",
                produto.getNome(), produto.getDescricao());

        try {
            Map<String, Object> requestBody = criarCorpoRequisicao(prompt);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(getUrlAi(), request, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");

            // 1. Verifica se há resposta textual
            if (!textNode.isTextual()) {
                throw new LudiBoxException("IA", "Resposta inesperada da IA", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // 2. Limpa o texto (remove ```json se existir)
            String texto = limparTexto(textNode.asText());

            // 3. Verifica se é um JSON válido
            if (!texto.startsWith("{") || !texto.endsWith("}")) {
                throw new LudiBoxException("IA", "Resposta inválida da IA", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // 4. Parseia o JSON
            JsonNode json = mapper.readTree(texto);

            // 5. Se houver violação, lança erro 422
            if (json.has("violation") && json.get("violation").asBoolean()) {
                String mensagem = json.has("message") ? json.get("message").asText() : "Conteúdo impróprio detectado";
                throw new LudiBoxException("IA", mensagem, HttpStatus.UNPROCESSABLE_ENTITY);
            }

            // 6. Se não houver violação, retorna o produto original
            return produto;

        } catch (LudiBoxException e) {
            throw e; // Re-lança exceções já tratadas
        } catch (Exception e) {
            log.error("Erro ao validar conteúdo do produto", e);
            throw new LudiBoxException("IA", "Erro interno ao validar conteúdo", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String limparTexto(String texto) {
        int inicio = texto.indexOf("{");
        int fim = texto.lastIndexOf("}");
        if (inicio != -1 && fim != -1 && inicio < fim) {
            return texto.substring(inicio, fim + 1).trim();
        }
        return texto.trim();
    }


    private Map<String, Object> criarCorpoRequisicao(String prompt) {
        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> user = Map.of(
                "role", "user",
                "parts", List.of(part)
        );

        return Map.of("contents", List.of(user));
    }


}
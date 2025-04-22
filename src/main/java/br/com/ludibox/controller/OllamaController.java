package br.com.ludibox.controller;

import br.com.ludibox.model.dto.ProdutoListarDto;
import br.com.ludibox.service.ProdutoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ollama")
@CrossOrigin("*")
public class OllamaController {

    private ChatClient chatClient;
    private ObjectMapper objectMapper;

    @Autowired
    private ProdutoService produtoService;

    @GetMapping("/moderacao/{mensagem}")
    public ResponseEntity<Boolean> verificarOfensa(@PathVariable String mensagem) {
        String prompt = """
                Responda apenas com "true" ou "false".
                A seguinte frase contém palavrões ou linguagem ofensiva?
                
                "%s"
                """.formatted(mensagem);

        // Tenta com IA
        String respostaIA = chatClient
                .prompt(prompt)
                .call()
                .content()
                .trim()
                .toLowerCase();

        boolean iaDetectou = respostaIA.contains("true");

        // Fallback com lista de palavrões
        boolean regexDetectou = contemPalavrao(mensagem);

        return ResponseEntity.ok(iaDetectou || regexDetectou);
    }

    private boolean contemPalavrao(String mensagem) {
        String[] palavroes = {
                "bosta", "merda", "porra", "caralho", "vai tomar no cu",
                "fdp", "filho da puta", "filho da mãe", "puta",
                "vagabunda", "arrombado", "otário", "babaca"
        };
        mensagem = mensagem.toLowerCase();
        return Arrays.stream(palavroes).anyMatch(mensagem::contains);
    }


    public OllamaController(OllamaChatModel chatModel) {
        this.chatClient = ChatClient.create(chatModel);
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping("/{message}")
    public ResponseEntity<String> getAnswer(@PathVariable String message) {
        List<ProdutoListarDto> produtos = produtoService.buscarTodos();

        Map<String, ProdutoListarDto> produtosPorNome = produtos.stream()
                .collect(Collectors.toMap(p -> p.getNome().toLowerCase(), p -> p));

        StringBuilder produtosFormatados = new StringBuilder();
        for (int i = 0; i < produtos.size(); i++) {
            ProdutoListarDto p = produtos.get(i);
            produtosFormatados.append(String.format(
                    "%d. Nome: %s\n",
                    i + 1, p.getNome()
            ));
        }

        String prompt = String.format("""
                Você é um assistente virtual que ajuda o usuário a encontrar produtos.
                
                IMPORTANTE: Você DEVE seguir estas regras:
                1. NUNCA invente produtos que não estão na lista abaixo
                2. NUNCA modifique os nomes, preços ou categorias dos produtos
                3. Se não encontrar produtos relevantes, responda "Nenhum produto encontrado para sua busca."
                
                Aqui está a lista COMPLETA de produtos disponíveis:
                %s
                
                Analise a mensagem do usuário e retorne APENAS produtos que existem na lista acima.
                Formate cada produto exatamente assim:
                - Produto: [nome exato]
                
                Mensagem do usuário:
                "%s"
                """, produtosFormatados.toString(), message);

        String resposta = chatClient.prompt(prompt).call().content();

        return ResponseEntity.ok(resposta);
    }

    private String validarResposta(String resposta, Map<String, ProdutoListarDto> produtosPorNome) {
        if (resposta == null || resposta.isBlank() ||
                resposta.contains("Nenhum produto encontrado para sua busca.")) {
            return "Nenhum produto encontrado para sua busca.";
        }

        Pattern pattern = Pattern.compile("Produto:\\s*(.+)");
        List<String> linhas = Arrays.stream(resposta.split("\n"))
                .filter(l -> l.trim().startsWith("-") || l.trim().startsWith("•"))
                .collect(Collectors.toList());

        List<String> produtosValidados = linhas.stream()
                .map(linha -> {
                    Matcher matcher = pattern.matcher(linha);
                    if (matcher.find()) {
                        String nome = matcher.group(1).trim().toLowerCase();
                        ProdutoListarDto produto = produtosPorNome.get(nome);
                        if (produto != null) {
                            return String.format("- Produto: %s", produto.getNome());
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (produtosValidados.isEmpty()) {
            return "Nenhum produto encontrado para sua busca.";
        }

        return "Produtos encontrados para sua busca:\n\n" + String.join("\n", produtosValidados);
    }
}
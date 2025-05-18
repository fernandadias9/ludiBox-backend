package br.com.ludibox.service;

import br.com.ludibox.auth.AuthenticationService;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.entity.Produto;
import br.com.ludibox.model.repository.ProdutoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ModeradorIA {

    String GEMINI_API_KEY = "AIzaSyCXcdo7jpFgdL8Mte5sn2Ig0lonXMLtDcE";

    String urlAi = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="+GEMINI_API_KEY;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private ImagemService imagemService;

    private static final int MAX_IMAGENS = 4;
    private static final long MAX_TAMANHO_IMAGEM = 2 * 1024 * 1024;



    public void salvarProdutoValidandoComIA(@Valid Produto produto, List<MultipartFile> imagens) throws LudiBoxException, IOException {
        Pessoa pessoaAutenticada = authService.getPessoaAutenticada();
        produto.setAnunciante(pessoaAutenticada);

        if (imagens != null && imagens.size() > MAX_IMAGENS) {
            throw new LudiBoxException("Imagens", "Número máximo de imagens excedido. Máximo permitido: " + MAX_IMAGENS, HttpStatus.BAD_REQUEST);
        }

        List<String> imagensBase64 = new ArrayList<>();
        if (imagens != null) {
            for (MultipartFile imagem : imagens) {
                if (imagem.getSize() > MAX_TAMANHO_IMAGEM) {
                    throw new LudiBoxException("Imagens", "Tamanho máximo da imagem excedido. Máximo permitido: " + MAX_TAMANHO_IMAGEM + " bytes", HttpStatus.BAD_REQUEST);
                }
                String base64Imagem = imagemService.processarImagem(imagem);
                imagensBase64.add(base64Imagem);
            }
        }
        produto.setImagens(imagensBase64);

        produtoRepository.save(produto);
    }

    public String validarProduto(Produto produto){
        String prompt = "Reescreva o nome e a descrição do produto para torná-los mais atrativos em um e-commerce. Retorne somente o JSON no formato: {\"nome\": \"...\", \"descricao\": \"...\"}. Produto: {\"nome\": \""
                + produto.getNome() + "\", \"descricao\": \"" + produto.getDescricao() + "\"}";

        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        List<Map<String, Object>> parts = new ArrayList<>();
        parts.add(part);

        Map<String, Object> user = new HashMap<>();
        user.put("role", "user");
        user.put("parts", parts);

        List<Map<String, Object>> contents = new ArrayList<>();
        contents.add(user);

        Map<String, Object> body = new HashMap<>();
        body.put("contents", contents);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(urlAi, request, String.class);

        return response.getBody();
    }
}

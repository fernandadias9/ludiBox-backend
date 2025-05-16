package br.com.ludibox.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ModeradorIA {

    String GEMINI_API_KEY = "AIzaSyCXcdo7jpFgdL8Mte5sn2Ig0lonXMLtDcE";

    String urlAi = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="+GEMINI_API_KEY;

    public String processarTexto(String mensagem){
        RestTemplate restTemplate = new RestTemplate();


        Map<String, Object> part = new HashMap<>();
        part.put("text", mensagem);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(urlAi, request, String.class);

        return response.getBody();
}


}

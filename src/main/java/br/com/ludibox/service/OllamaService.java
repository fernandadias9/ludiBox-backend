package br.com.ludibox.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class OllamaService {

    private final ChatClient chatClient;

    public OllamaService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String tellMeAJoke() {
        return chatClient.prompt("Tell me a joke").call().content();
    }
}

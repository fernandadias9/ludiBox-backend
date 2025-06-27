package br.com.ludibox.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailService emailService;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> mensagemCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testEnviarNovaSenha() {
        String destino = "usuario@teste.com";
        String novaSenha = "senha123";

        emailService.enviarNovaSenha(destino, novaSenha);

        verify(javaMailSender).send(mensagemCaptor.capture());

        SimpleMailMessage mensagem = mensagemCaptor.getValue();
        assertEquals(destino, mensagem.getTo()[0]);
        assertEquals("Recuperação de Senha - Ludibox", mensagem.getSubject());
        assertTrue(mensagem.getText().contains(novaSenha));
        assertTrue(mensagem.getText().contains("Recomendamos que você altere essa senha"));
    }

    @Test
    void testEnviarAnuncioBloqueado() {
        String destino = "usuario@teste.com";
        String nomeProduto = "Bicicleta Infantil";

        emailService.enviarAnuncioBloqueado(destino, nomeProduto);

        verify(javaMailSender).send(mensagemCaptor.capture());

        SimpleMailMessage mensagem = mensagemCaptor.getValue();
        assertEquals(destino, mensagem.getTo()[0]);
        assertEquals("Seu anúncio foi bloqueado - Ludibox", mensagem.getSubject());
        assertTrue(mensagem.getText().contains(nomeProduto));
        assertTrue(mensagem.getText().contains("foi bloqueado por violar as políticas"));
    }
}

package br.com.ludibox.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender envioEmail;

    public void enviarNovaSenha(String destino, String novaSenha) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setTo(destino);
        mensagem.setSubject("Recuperação de Senha - Ludibox");
        mensagem.setText("Olá!\n\nSua nova senha é: " + novaSenha +
                "\n\nRecomendamos que você altere essa senha após fazer login no sistema.\n\nEquipe Ludibox");
        envioEmail.send(mensagem);
    }
}

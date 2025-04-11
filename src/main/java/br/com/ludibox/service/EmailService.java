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
        mensagem.setText("Olá!\n\nConforme solicitado, sua nova senha de acesso ao sistema Ludibox é:\n\n" +
                novaSenha + "\n\n" +
                "Por motivos de segurança, recomendamos que você altere essa senha assim que fizer login.\n\n" +
                "Atenciosamente,\nEquipe Ludibox!");
        envioEmail.send(mensagem);
    }
}

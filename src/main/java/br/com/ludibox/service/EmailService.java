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
    public void enviarAnuncioBloqueado(String destino, String nomeProduto) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setTo(destino);
        mensagem.setSubject("Seu anúncio foi bloqueado - Ludibox");
        mensagem.setText("Olá,\n\nO seu anúncio \"" + nomeProduto + "\" foi bloqueado por violar as políticas da Ludibox.\n\nSe quiser mais informações, entre em contato com o suporte.\n\nAtenciosamente,\n\nEquipe Ludibox");
        envioEmail.send(mensagem);
    }
}

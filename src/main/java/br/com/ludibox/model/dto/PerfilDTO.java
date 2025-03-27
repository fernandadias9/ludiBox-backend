package br.com.ludibox.model.dto;

import lombok.Data;
import org.springframework.context.annotation.Bean;

@Data
public class PerfilDTO {
    private Integer id;
    private String nome;
    private String imagemUsuarioEmBase64;

}

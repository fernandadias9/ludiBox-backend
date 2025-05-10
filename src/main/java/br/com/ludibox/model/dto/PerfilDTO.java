package br.com.ludibox.model.dto;

import lombok.Data;

@Data
public class PerfilDTO {
    private Integer id;
    private String nome;
    private String imagemUsuarioEmBase64;
    private String email;
    private String telefone;
    private String senha;
    private String valorDocumento;
}

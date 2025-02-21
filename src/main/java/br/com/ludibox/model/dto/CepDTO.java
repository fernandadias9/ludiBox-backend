package br.com.ludibox.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class CepDTO {

    private String cep;

    private String logadouro;

    private String complemento;

    private String bairro;

    private String localidade;

    private String uf;

    private String estado;

    private Integer ibge;

    private Integer gia;

    private Integer ddd;

    private Integer siafi;

}

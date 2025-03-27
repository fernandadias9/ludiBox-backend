package br.com.ludibox.model.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProdutoListarDto {
    private String nome;
    private LocalDate dataCadastro;
    private int altura;
    private int largura;
    private int comprimento;
    private String descricao;
    private Integer estoque;
    private Double preco;
    private List<LocalDate> datasIndisponiveis;
    private Integer idAnunciante;
    private String nomeAnunciante;
    private String imagem;
}

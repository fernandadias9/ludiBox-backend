package br.com.ludibox.model.dto;

import br.com.ludibox.model.enums.StatusProduto;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProdutoDetalheDto {
    private Integer id;
    private String nome;
    private LocalDate dataCadastro;
    private int altura;
    private int largura;
    private int comprimento;
    private int pesoSuportado;
    private String descricao;
    private Double preco;
    private List<LocalDate> datasIndisponiveis;
    private Integer idAnunciante;
    private String nomeAnunciante;
    private String imagemAnunciante;
    private List<String> imagens;
}

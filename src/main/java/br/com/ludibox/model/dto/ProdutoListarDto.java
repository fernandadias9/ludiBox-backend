package br.com.ludibox.model.dto;

import br.com.ludibox.model.enums.StatusProduto;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProdutoListarDto {
    private Integer id;
    private String nome;
    private Double preco;
    private Integer idAnunciante;
    private String nomeAnunciante;
    private String imagemAnunciante;
    private String imagem;
    private StatusProduto status;
}

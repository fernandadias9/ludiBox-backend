package br.com.ludibox.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AvaliacaoRequestDTO {

    @NotNull
    private Integer produtoLocacaoId;

    @Min(1) @Max(5)
    private int estrelas;

    @Size(max = 300)
    private String comentario;

    public Integer getProdutoLocacaoId() {
        return produtoLocacaoId;
    }

    public void setProdutoLocacaoId(Integer produtoLocacaoId) {
        this.produtoLocacaoId = produtoLocacaoId;
    }

    public int getEstrelas() {
        return estrelas;
    }

    public void setEstrelas(int estrelas) {
        this.estrelas = estrelas;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}

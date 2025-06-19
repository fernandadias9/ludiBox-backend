package br.com.ludibox.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "avaliacao")
public class Avaliacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Min(1) @Max(5)
    private int estrelas;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_locacao_id")
    private ProdutoLocacao produtoLocacao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "avaliador_id")
    private Pessoa avaliador;

    private LocalDate dataAvaliacao = LocalDate.now();

    @NotNull
    private Boolean ativo = true;
}

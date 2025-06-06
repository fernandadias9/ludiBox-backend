package br.com.ludibox.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Data
@Entity
public class ProdutoLocacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "locacao_id")
    private Locacao locacao;

    @NotNull
    private LocalDate dataInicio;

    @NotNull
    private LocalDate dataFim;

    @NotNull
    @PositiveOrZero
    private Double valorDiario;

    public long getDiasLocados() {
        return ChronoUnit.DAYS.between(dataInicio, dataFim) + 1;
    }

    public double getTotalProduto() {
        return getDiasLocados() * valorDiario;
    }
}

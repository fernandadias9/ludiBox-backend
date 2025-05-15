package br.com.ludibox.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Cancelamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private LocalDateTime dataHoraCancelamento;

    @NotNull
    private String motivo;

    @ManyToOne
    @JoinColumn(name = "locacao_id")
    private Locacao locacao;

    @NotNull
    private Double multa;
}

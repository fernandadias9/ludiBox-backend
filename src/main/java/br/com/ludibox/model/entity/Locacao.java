package br.com.ludibox.model.entity;

import br.com.ludibox.model.enums.StatusLocacao;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Locacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime dataHoraEfetuada;

    @OneToMany(mappedBy = "locacao", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProdutoLocacao> produtos = new ArrayList<>();

    @PositiveOrZero
    private Double valorTotal;

    @ManyToOne
    @JoinColumn(name = "locador_id")
    private Pessoa locador;

    @Enumerated(EnumType.STRING)
    private StatusLocacao status;

    private Boolean cancelado = false;

    private LocalDateTime dataHoraCancelamento;

    private String formaPagamento;

    private LocalDateTime dataHoraPagamento;

    @ManyToOne
    @JoinColumn(name = "endereco_entrega_id")
    private Endereco enderecoEntrega;
}

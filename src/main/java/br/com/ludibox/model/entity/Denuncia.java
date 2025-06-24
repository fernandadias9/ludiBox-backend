package br.com.ludibox.model.entity;

import br.com.ludibox.model.enums.EnumStatusDenuncia;
import br.com.ludibox.model.enums.EnumMotivoDenuncia;
import br.com.ludibox.model.enums.EnumStatusProdutoDenunciado;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "denuncia")
public class Denuncia {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shared_seq")
    @SequenceGenerator(name = "shared_seq", sequenceName = "shared_seq", allocationSize = 1)
    private Integer id;

    @NotNull(message = "Motivo da denúncia é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnumMotivoDenuncia motivo;

    @Lob
    @Column(length = 100)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnumStatusDenuncia status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "denunciante_id", nullable = false)
    private Pessoa denunciante;

    @Column(name = "dataCriacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_produto_denunciado")
    private EnumStatusProdutoDenunciado StatusProdutoDenunciado;

    @PrePersist
    protected void onCreate() {
        this.status = EnumStatusDenuncia.NOVO;
        this.dataCriacao = LocalDateTime.now();
    }
}

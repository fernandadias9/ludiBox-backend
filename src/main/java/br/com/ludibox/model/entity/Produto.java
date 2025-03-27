package br.com.ludibox.model.entity;

import br.com.ludibox.model.enums.StatusProduto;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shared_seq")
    @SequenceGenerator(name = "shared_seq", sequenceName = "shared_seq", allocationSize = 1)
    private Integer id;

    @NotBlank
    private String nome;

    @CreationTimestamp
    private LocalDate dataCadastro;

    private int altura;

    private int largura;

    private int comprimento;

    @NotBlank
    @Size(min = 1, max = 300)
    private String descricao;

    @NotNull
    @Min(0)
    private Integer estoque;

    @NotNull
    @Positive
    private Double preco;

    @ElementCollection
    private List<LocalDate> datasIndisponiveis;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Pessoa anunciante;

    private StatusProduto status = StatusProduto.ATIVO;

    @ManyToOne
    @JoinColumn(name = "id_endereco")
    private Endereco endereco;

    @ElementCollection
    @CollectionTable(name = "produto_imagens", joinColumns = @JoinColumn(name = "produto_id"))
    @Column(name = "imagem", length = 10485760)
    private List<String> imagens = new ArrayList<>();
}

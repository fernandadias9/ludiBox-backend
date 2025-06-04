package br.com.ludibox.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pessoa_excluida")
public class PessoaExcluida {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuario_excluido_seq")
    @SequenceGenerator(name = "usuario_excluido_seq", sequenceName = "usuario_excluido_seq", allocationSize = 1)
    private Integer id;

    @Column(nullable = false)
    private String cpfOuCnpj;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalDateTime dataExclusao;
}

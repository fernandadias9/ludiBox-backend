package br.com.ludibox.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "pagamento_anunciante")
public class PagamentosAnunciante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDate dataLimiteLiberacao;

    private String nomeAnunciante;

    private String tipoChavePix;

    private String valorChavePix;

    private BigDecimal valor;

    private boolean pago = false;
}

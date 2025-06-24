package br.com.ludibox.model.dto;

import br.com.ludibox.model.enums.EnumMotivoDenuncia;
import br.com.ludibox.model.enums.EnumStatusDenuncia;
import br.com.ludibox.model.enums.EnumStatusProdutoDenunciado;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DenunciaListarDTO {
    private Integer id;
    private EnumMotivoDenuncia motivo;
    private String descricao;
    private EnumStatusDenuncia status;
    private LocalDateTime dataCriacao;
    private DenuncianteDTO denunciante;
    private String nomeDenunciado;
    private Integer produtoId;
    private EnumStatusProdutoDenunciado statusProdutoDenunciado;
}

package br.com.ludibox.model.dto;

import br.com.ludibox.model.enums.EnumMotivoDenuncia;
import br.com.ludibox.model.enums.EnumStatusProdutoDenunciado;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DenunciaDTO {

    private EnumMotivoDenuncia motivo;
    private String descricao;
    private Integer produtoId;
    private Integer denuncianteId;
    private EnumStatusProdutoDenunciado statusProduto;
}

package br.com.ludibox.model.dto;

import br.com.ludibox.model.enums.EnumStatusDenuncia;
import lombok.Data;

@Data
public class StatusDTO {
    private EnumStatusDenuncia status;
}
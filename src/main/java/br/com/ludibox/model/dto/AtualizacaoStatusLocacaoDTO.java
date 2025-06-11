package br.com.ludibox.model.dto;

import br.com.ludibox.model.enums.StatusLocacao;

public class AtualizacaoStatusLocacaoDTO {

    private StatusLocacao status;

    public String getStatus() {
        return String.valueOf(status);
    }

    public void setStatus(StatusLocacao status) {
        this.status = status;
    }
}

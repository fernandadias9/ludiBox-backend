package br.com.ludibox.model.interfaces;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Produto;

public interface ValidadorConteudoIA {
    void validar(Produto produto) throws LudiBoxException;
}

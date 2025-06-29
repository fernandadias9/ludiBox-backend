package br.com.ludibox.service.IA;

import br.com.ludibox.model.entity.Produto;
import br.com.ludibox.model.interfaces.ValidadorConteudoIA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ValidadorConteudoService {

    private final List<ValidadorConteudoIA> validadores;

    @Autowired
    public ValidadorConteudoService(List<ValidadorConteudoIA> validadores) {
        this.validadores = validadores;
    }

    public Produto validar(Produto produto) {
        validadores.forEach(validador -> validador.validar(produto));
        return produto;
    }
}

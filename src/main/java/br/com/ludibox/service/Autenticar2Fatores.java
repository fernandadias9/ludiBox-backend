package br.com.ludibox.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class Autenticar2Fatores {
    private final Map<String, String> codigosAtivos = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public String gerarCodigo(String email) {
        String codigo = String.format("%06d", random.nextInt(1000000));
        codigosAtivos.put(email, codigo);
        return codigo;
    }

    public boolean validarCodigo(String email, String codigo) {
        return codigo.equals(codigosAtivos.get(email));
    }

    public void removerCodigo(String email) {
        codigosAtivos.remove(email);
    }
}

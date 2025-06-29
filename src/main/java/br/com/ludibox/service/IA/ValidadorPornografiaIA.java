package br.com.ludibox.service.IA;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Produto;
import br.com.ludibox.model.interfaces.ValidadorConteudoIA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ValidadorPornografiaIA implements ValidadorConteudoIA {

    @Autowired
    private ModeradorIAClient iaClient;

    @Override
    public void validar(Produto produto) throws LudiBoxException {
        String prompt = String.format(
                "Analise o nome e a descrição do produto para verificar se contém conteúdo pornográfico ou sexualmente explícito. Retorne {\"violation\": true, \"message\": \"Conteúdo pornográfico\"} se encontrar, caso contrário {\"violation\": false}.\n" +
                        "Produto: {\"nome\": \"%s\", \"descricao\": \"%s\"}",
                produto.getNome(), produto.getDescricao());

        iaClient.enviarPromptEValidar(prompt, "Pornografia");
    }
}

package br.com.ludibox.service;

import br.com.ludibox.LudiboxApplication;
import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.dto.CepDTO;
import br.com.ludibox.model.entity.Endereco;
import io.jsonwebtoken.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CepService {

    String urlApiCep = "https://viacep.com.br/ws/";

    public Endereco validarCep(Endereco endereco) throws LudiBoxException{
        CepDTO cepDTO = new CepDTO();
        try {
            String url = urlApiCep + endereco.getCep() + "/json";
            RestTemplate restTemplate = new RestTemplate();
            cepDTO = restTemplate.getForEntity(url, CepDTO.class).getBody();
        }catch (Exception e){
            throw new LudiBoxException("CEP inválido");
        }
        endereco.setCidade(cepDTO.getLocalidade());
        endereco.setEstado(cepDTO.getEstado());
        return endereco;
    }

    public CepDTO buscarEnderecoPorCep(Integer cep) throws LudiBoxException {
        CepDTO cepDTO = new CepDTO();

        try {
            String url = urlApiCep + cep + "/json";
            RestTemplate restTemplate = new RestTemplate();
            cepDTO = restTemplate.getForEntity(url, CepDTO.class).getBody();
        } catch (Exception e){
            throw  new LudiBoxException("CEP invalido");
        }

        return cepDTO;
    }

}

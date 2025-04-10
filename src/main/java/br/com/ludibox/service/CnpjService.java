package br.com.ludibox.service;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.dto.CnpjDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class CnpjService {

    String urlApiCnpj = "https://receitaws.com.br/v1/cnpj/";


   /* public void validarCepAndCnpj(PessoaJuridica pj) throws LudiBoxException {
        RestTemplate restTemplate = new RestTemplate();
        CnpjDTO cnpjDTO = new CnpjDTO();
        String cepTratado = new String();
        String cepConvertido = new String();

        try{
            String url = urlApiCnpj + pj.getCnpj();
            cnpjDTO = restTemplate.getForEntity(url, CnpjDTO.class).getBody();
        } catch (RestClientException e) {
            throw new RuntimeException(e);
        }

        cepTratado = cnpjDTO.getCep().replaceAll("\\D", "");
        cepConvertido = String.valueOf(pj.getEndereco().getCep());

        if (!cepTratado.equals(cepConvertido)){
            throw new LudiBoxException("CEP (" + pj.getEndereco().getCep() +") não pertence ao CNPJ ("+ pj.getCnpj() +") informado!", HttpStatus.BAD_REQUEST);
        }
    }*/


    public CnpjDTO buscarPjPorCnpj(String cnpj) {
        RestTemplate restTemplate = new RestTemplate();
        CnpjDTO cnpjDTO = new CnpjDTO();

        try {
            String url = urlApiCnpj + cnpj;
            cnpjDTO = restTemplate.getForEntity(url, CnpjDTO.class).getBody();
        } catch (RestClientException e) {
            throw new RuntimeException(e);
        }
        return cnpjDTO;
    }
}

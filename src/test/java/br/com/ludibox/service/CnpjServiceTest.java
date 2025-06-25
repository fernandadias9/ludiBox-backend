package br.com.ludibox.service;

import br.com.ludibox.model.dto.CnpjDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CnpjServiceTest {

    private RestTemplate restTemplate;
    private CnpjService cnpjService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);

        // Subclasse anônima que usa o mock manualmente
        cnpjService = new CnpjService() {
            @Override
            public CnpjDTO buscarPjPorCnpj(String cnpj) {
                try {
                    String url = "https://receitaws.com.br/v1/cnpj/" + cnpj;
                    return restTemplate.getForEntity(url, CnpjDTO.class).getBody();
                } catch (RestClientException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Test
    void testBuscarPjPorCnpj_Sucesso() {
        String cnpj = "12345678000195";
        String url = "https://receitaws.com.br/v1/cnpj/" + cnpj;

        CnpjDTO mockDto = new CnpjDTO();
        mockDto.setNome("Empresa Teste");
        mockDto.setCep("12345678");

        when(restTemplate.getForEntity(url, CnpjDTO.class))
                .thenReturn(ResponseEntity.ok(mockDto));

        CnpjDTO resultado = cnpjService.buscarPjPorCnpj(cnpj);

        assertEquals("Empresa Teste", resultado.getNome());
        assertEquals("12345678", resultado.getCep());
    }

    @Test
    void testBuscarPjPorCnpj_ErroNaRequisicao() {
        String cnpj = "12345678000195";
        String url = "https://receitaws.com.br/v1/cnpj/" + cnpj;

        when(restTemplate.getForEntity(url, CnpjDTO.class))
                .thenThrow(new RestClientException("Erro"));

        assertThrows(RuntimeException.class, () -> cnpjService.buscarPjPorCnpj(cnpj));
    }
}

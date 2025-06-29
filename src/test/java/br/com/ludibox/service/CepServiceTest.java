package br.com.ludibox.service;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.dto.CepDTO;
import br.com.ludibox.model.entity.Endereco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CepServiceTest {

    @InjectMocks
    private CepService cepService;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testValidarCep_ComSucesso() {
        Endereco endereco = new Endereco();
        endereco.setCep(12345678);

        CepDTO cepDTO = new CepDTO();
        cepDTO.setLocalidade("São Paulo");
        cepDTO.setEstado("SP");

        String url = "https://viacep.com.br/ws/12345678/json";

        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ResponseEntity<CepDTO> response = ResponseEntity.ok(cepDTO);
        when(mockRestTemplate.getForEntity(url, CepDTO.class)).thenReturn(response);

        CepService service = new CepService() {
            @Override
            public Endereco validarCep(Endereco endereco) throws LudiBoxException {
                try {
                    CepDTO dto = mockRestTemplate.getForEntity(url, CepDTO.class).getBody();
                    endereco.setCidade(dto.getLocalidade());
                    endereco.setEstado(dto.getEstado());
                    return endereco;
                } catch (Exception e) {
                    throw new LudiBoxException("CEP", "Valor inválido", HttpStatus.BAD_REQUEST);
                }
            }
        };

        Endereco resultado = service.validarCep(endereco);

        assertEquals("São Paulo", resultado.getCidade());
        assertEquals("SP", resultado.getEstado());
    }

    @Test
    void testValidarCep_ComErro() {
        Endereco endereco = new Endereco();
        endereco.setCep(0);

        CepService service = new CepService() {
            @Override
            public Endereco validarCep(Endereco endereco) throws LudiBoxException {
                throw new LudiBoxException("CEP", "Valor inválido", HttpStatus.BAD_REQUEST);
            }
        };

        assertThrows(LudiBoxException.class, () -> service.validarCep(endereco));
    }

    @Test
    void testBuscarEnderecoPorCep_ComSucesso() {
        CepDTO cepDTO = new CepDTO();
        cepDTO.setEstado("RJ");
        cepDTO.setLocalidade("Rio de Janeiro");

        Integer cep = 12345678;
        String url = "https://viacep.com.br/ws/" + cep + "/json";

        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ResponseEntity<CepDTO> response = ResponseEntity.ok(cepDTO);
        when(mockRestTemplate.getForEntity(url, CepDTO.class)).thenReturn(response);

        CepService service = new CepService() {
            @Override
            public CepDTO buscarEnderecoPorCep(Integer cep) throws LudiBoxException {
                return mockRestTemplate.getForEntity(url, CepDTO.class).getBody();
            }
        };

        CepDTO resultado = service.buscarEnderecoPorCep(cep);

        assertEquals("RJ", resultado.getEstado());
        assertEquals("Rio de Janeiro", resultado.getLocalidade());
    }

    @Test
    void testBuscarEnderecoPorCep_ComErro() {
        CepService service = new CepService() {
            @Override
            public CepDTO buscarEnderecoPorCep(Integer cep) throws LudiBoxException {
                throw new LudiBoxException("CEP", "valor inválido", HttpStatus.BAD_REQUEST);
            }
        };

        assertThrows(LudiBoxException.class, () -> service.buscarEnderecoPorCep(123));
    }
}

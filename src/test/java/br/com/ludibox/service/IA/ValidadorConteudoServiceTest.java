package br.com.ludibox.service.IA;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Produto;
import br.com.ludibox.model.interfaces.ValidadorConteudoIA;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidadorConteudoServiceTest {

    @InjectMocks
    private ValidadorConteudoService validadorConteudoService;

    @Mock
    private ValidadorConteudoIA validador1;

    @Mock
    private ValidadorConteudoIA validador2;

    @Mock
    private ValidadorConteudoIA validador3;

    private Produto produto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        produto = new Produto();
        produto.setId(1);
        produto.setNome("Produto Teste");
        produto.setDescricao("Descrição do produto teste");
    }

    @Test
    void testValidar_ComMultiplosValidadores_ComSucesso() {
        List<ValidadorConteudoIA> validadores = Arrays.asList(validador1, validador2, validador3);
        validadorConteudoService = new ValidadorConteudoService(validadores);

        doNothing().when(validador1).validar(produto);
        doNothing().when(validador2).validar(produto);
        doNothing().when(validador3).validar(produto);

        Produto resultado = validadorConteudoService.validar(produto);

        assertEquals(produto, resultado);
        verify(validador1).validar(produto);
        verify(validador2).validar(produto);
        verify(validador3).validar(produto);
    }

    @Test
    void testValidar_ComUmValidador_ComSucesso() {
        List<ValidadorConteudoIA> validadores = Arrays.asList(validador1);
        validadorConteudoService = new ValidadorConteudoService(validadores);

        doNothing().when(validador1).validar(produto);

        Produto resultado = validadorConteudoService.validar(produto);

        assertEquals(produto, resultado);
        verify(validador1).validar(produto);
    }

    @Test
    void testValidar_SemValidadores_ComSucesso() {
        List<ValidadorConteudoIA> validadores = Collections.emptyList();
        validadorConteudoService = new ValidadorConteudoService(validadores);

        Produto resultado = validadorConteudoService.validar(produto);

        assertEquals(produto, resultado);
        verifyNoInteractions(validador1, validador2, validador3);
    }

    @Test
    void testValidar_PrimeiroValidadorLancaExcecao_DevePararExecucao() {
        List<ValidadorConteudoIA> validadores = Arrays.asList(validador1, validador2, validador3);
        validadorConteudoService = new ValidadorConteudoService(validadores);

        LudiBoxException exception = new LudiBoxException("Erro", "Conteúdo inválido", HttpStatus.BAD_REQUEST);
        doThrow(exception).when(validador1).validar(produto);

        LudiBoxException thrownException = assertThrows(LudiBoxException.class,
                () -> validadorConteudoService.validar(produto));

        assertEquals(exception, thrownException);
        verify(validador1).validar(produto);
        verifyNoInteractions(validador2, validador3);
    }

    @Test
    void testValidar_SegundoValidadorLancaExcecao_PrimeiroDeveExecutar() {
        List<ValidadorConteudoIA> validadores = Arrays.asList(validador1, validador2, validador3);
        validadorConteudoService = new ValidadorConteudoService(validadores);

        LudiBoxException exception = new LudiBoxException("Erro", "Conteúdo inválido", HttpStatus.BAD_REQUEST);
        doNothing().when(validador1).validar(produto);
        doThrow(exception).when(validador2).validar(produto);

        LudiBoxException thrownException = assertThrows(LudiBoxException.class,
                () -> validadorConteudoService.validar(produto));

        assertEquals(exception, thrownException);
        verify(validador1).validar(produto);
        verify(validador2).validar(produto);
        verifyNoInteractions(validador3);
    }

    @Test
    void testValidar_ComProdutoNulo_DevePassarParaValidadores() {
        List<ValidadorConteudoIA> validadores = Arrays.asList(validador1);
        validadorConteudoService = new ValidadorConteudoService(validadores);

        doNothing().when(validador1).validar(null);

        Produto resultado = validadorConteudoService.validar(null);

        assertNull(resultado);
        verify(validador1).validar(null);
    }

    @Test
    void testValidar_ValidadorLancaRuntimeException_DevePropagarExcecao() {
        List<ValidadorConteudoIA> validadores = Arrays.asList(validador1);
        validadorConteudoService = new ValidadorConteudoService(validadores);

        RuntimeException exception = new RuntimeException("Erro inesperado");
        doThrow(exception).when(validador1).validar(produto);

        RuntimeException thrownException = assertThrows(RuntimeException.class,
                () -> validadorConteudoService.validar(produto));

        assertEquals(exception, thrownException);
        verify(validador1).validar(produto);
    }
}
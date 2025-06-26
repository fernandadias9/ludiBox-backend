package br.com.ludibox.service.IA;

import br.com.ludibox.exception.LudiBoxException;
import br.com.ludibox.model.entity.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidadorPornografiaIATest {

    @InjectMocks
    private ValidadorPornografiaIA validadorPornografiaIA;

    @Mock
    private ModeradorIAClient iaClient;

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
    void testValidar_SemConteudoPornografico_ComSucesso() {
        String expectedPrompt = String.format(
                "Analise o nome e a descrição do produto para verificar se contém conteúdo pornográfico ou sexualmente explícito. Retorne {\"violation\": true, \"message\": \"Conteúdo pornográfico\"} se encontrar, caso contrário {\"violation\": false}.\n" +
                        "Produto: {\"nome\": \"%s\", \"descricao\": \"%s\"}",
                produto.getNome(), produto.getDescricao());

        doNothing().when(iaClient).enviarPromptEValidar(expectedPrompt, "Pornografia");

        assertDoesNotThrow(() -> validadorPornografiaIA.validar(produto));

        verify(iaClient).enviarPromptEValidar(expectedPrompt, "Pornografia");
    }

    @Test
    void testValidar_ComConteudoPornografico_DeveLancarExcecao() {
        produto.setNome("Produto adulto");
        produto.setDescricao("Conteúdo para adultos com material explícito");

        String expectedPrompt = String.format(
                "Analise o nome e a descrição do produto para verificar se contém conteúdo pornográfico ou sexualmente explícito. Retorne {\"violation\": true, \"message\": \"Conteúdo pornográfico\"} se encontrar, caso contrário {\"violation\": false}.\n" +
                        "Produto: {\"nome\": \"%s\", \"descricao\": \"%s\"}",
                produto.getNome(), produto.getDescricao());

        LudiBoxException exception = new LudiBoxException("Pornografia", "Conteúdo pornográfico", HttpStatus.UNPROCESSABLE_ENTITY);
        doThrow(exception).when(iaClient).enviarPromptEValidar(expectedPrompt, "Pornografia");

        LudiBoxException thrownException = assertThrows(LudiBoxException.class,
                () -> validadorPornografiaIA.validar(produto));

        assertEquals(exception, thrownException);
        verify(iaClient).enviarPromptEValidar(expectedPrompt, "Pornografia");
    }

    @Test
    void testValidar_ComNomeNulo_DeveGerarPromptCorreto() {
        produto.setNome(null);
        produto.setDescricao("Descrição teste");

        String expectedPrompt = String.format(
                "Analise o nome e a descrição do produto para verificar se contém conteúdo pornográfico ou sexualmente explícito. Retorne {\"violation\": true, \"message\": \"Conteúdo pornográfico\"} se encontrar, caso contrário {\"violation\": false}.\n" +
                        "Produto: {\"nome\": \"%s\", \"descricao\": \"%s\"}",
                "null", produto.getDescricao());

        doNothing().when(iaClient).enviarPromptEValidar(expectedPrompt, "Pornografia");

        assertDoesNotThrow(() -> validadorPornografiaIA.validar(produto));

        verify(iaClient).enviarPromptEValidar(expectedPrompt, "Pornografia");
    }

    @Test
    void testValidar_ComDescricaoNula_DeveGerarPromptCorreto() {
        produto.setNome("Produto Teste");
        produto.setDescricao(null);

        String expectedPrompt = String.format(
                "Analise o nome e a descrição do produto para verificar se contém conteúdo pornográfico ou sexualmente explícito. Retorne {\"violation\": true, \"message\": \"Conteúdo pornográfico\"} se encontrar, caso contrário {\"violation\": false}.\n" +
                        "Produto: {\"nome\": \"%s\", \"descricao\": \"%s\"}",
                produto.getNome(), "null");

        doNothing().when(iaClient).enviarPromptEValidar(expectedPrompt, "Pornografia");

        assertDoesNotThrow(() -> validadorPornografiaIA.validar(produto));

        verify(iaClient).enviarPromptEValidar(expectedPrompt, "Pornografia");
    }

    @Test
    void testValidar_ComCaracteresEspeciais_DeveGerarPromptCorreto() {
        produto.setNome("Produto \"especial\" com 'aspas'");
        produto.setDescricao("Descrição com\nquebra de linha e\ttab");

        String expectedPrompt = String.format(
                "Analise o nome e a descrição do produto para verificar se contém conteúdo pornográfico ou sexualmente explícito. Retorne {\"violation\": true, \"message\": \"Conteúdo pornográfico\"} se encontrar, caso contrário {\"violation\": false}.\n" +
                        "Produto: {\"nome\": \"%s\", \"descricao\": \"%s\"}",
                produto.getNome(), produto.getDescricao());

        doNothing().when(iaClient).enviarPromptEValidar(expectedPrompt, "Pornografia");

        assertDoesNotThrow(() -> validadorPornografiaIA.validar(produto));

        verify(iaClient).enviarPromptEValidar(expectedPrompt, "Pornografia");
    }

    @Test
    void testValidar_ErroInternoIA_DeveLancarExcecao() {
        String expectedPrompt = String.format(
                "Analise o nome e a descrição do produto para verificar se contém conteúdo pornográfico ou sexualmente explícito. Retorne {\"violation\": true, \"message\": \"Conteúdo pornográfico\"} se encontrar, caso contrário {\"violation\": false}.\n" +
                        "Produto: {\"nome\": \"%s\", \"descricao\": \"%s\"}",
                produto.getNome(), produto.getDescricao());

        LudiBoxException exception = new LudiBoxException("Pornografia", "Erro interno ao validar com IA", HttpStatus.INTERNAL_SERVER_ERROR);
        doThrow(exception).when(iaClient).enviarPromptEValidar(expectedPrompt, "Pornografia");

        LudiBoxException thrownException = assertThrows(LudiBoxException.class,
                () -> validadorPornografiaIA.validar(produto));

        assertEquals(exception, thrownException);
        verify(iaClient).enviarPromptEValidar(expectedPrompt, "Pornografia");
    }

    @Test
    void testValidar_ComProdutoNulo_DeveLancarNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> validadorPornografiaIA.validar(null));

        verifyNoInteractions(iaClient);
    }

    @Test
    void testValidar_ComTextoLongo_DeveGerarPromptCorreto() {
        produto.setNome("Produto com nome muito longo que pode conter várias palavras e caracteres especiais !@#$%^&*()");
        produto.setDescricao("Descrição muito longa que pode conter múltiplas linhas\ne vários parágrafos com informações detalhadas sobre o produto, incluindo especificações técnicas, modo de uso, cuidados especiais e outras informações relevantes para o usuário final.");

        String expectedPrompt = String.format(
                "Analise o nome e a descrição do produto para verificar se contém conteúdo pornográfico ou sexualmente explícito. Retorne {\"violation\": true, \"message\": \"Conteúdo pornográfico\"} se encontrar, caso contrário {\"violation\": false}.\n" +
                        "Produto: {\"nome\": \"%s\", \"descricao\": \"%s\"}",
                produto.getNome(), produto.getDescricao());

        doNothing().when(iaClient).enviarPromptEValidar(expectedPrompt, "Pornografia");

        assertDoesNotThrow(() -> validadorPornografiaIA.validar(produto));

        verify(iaClient).enviarPromptEValidar(expectedPrompt, "Pornografia");
    }
}
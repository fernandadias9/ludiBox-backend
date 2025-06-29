package br.com.ludibox.model.seletor;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class BaseSeletorTest {

    // Como BaseSeletor é abstrata, criamos uma classe concreta simples para testar
    static class BaseSeletorImpl extends BaseSeletor {}

    private BaseSeletorImpl seletor;

    @BeforeEach
    void setup() {
        seletor = new BaseSeletorImpl();
    }

    @Test
    void testTemPaginacao_LimiteEPaginaPositivos_RetornaTrue() {
        seletor.setLimite(10);
        seletor.setPagina(2);
        assertTrue(seletor.temPaginacao());
    }

    @Test
    void testTemPaginacao_LimiteZeroOuPaginaZero_RetornaFalse() {
        seletor.setLimite(0);
        seletor.setPagina(1);
        assertFalse(seletor.temPaginacao());

        seletor.setLimite(1);
        seletor.setPagina(0);
        assertFalse(seletor.temPaginacao());

        seletor.setLimite(0);
        seletor.setPagina(0);
        assertFalse(seletor.temPaginacao());
    }

    @Test
    void testStringValida_RetornaTrue() {
        assertTrue(seletor.stringValida("texto"));
        assertTrue(seletor.stringValida("  texto com espaço  "));
    }

    @Test
    void testStringValida_RetornaFalse() {
        assertFalse(seletor.stringValida(null));
        assertFalse(seletor.stringValida(""));
        assertFalse(seletor.stringValida("    "));
    }

    @Test
    void testGetOffset_CalculaCorretamente() {
        seletor.setLimite(10);
        seletor.setPagina(1);
        assertEquals(0, seletor.getOffset());

        seletor.setPagina(3);
        assertEquals(20, seletor.getOffset());
    }

    // Testes para o método estático aplicarFiltroPeriodo

    @Test
    void testAplicarFiltroPeriodo_DataInicialEDataFinal_NenhumaNula() {
        Root<?> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        List<Predicate> predicates = new ArrayList<>();

        LocalDate dataInicial = LocalDate.of(2023, 1, 1);
        LocalDate dataFinal = LocalDate.of(2023, 1, 31);
        String atributo = "dataCriacao";

        // Mock dos métodos do CriteriaBuilder e Root
        when(root.get(atributo)).thenReturn(mock(Path.class));
        Predicate pred = mock(Predicate.class);
        when(cb.between(any(), eq(dataInicial), eq(dataFinal))).thenReturn(pred);

        BaseSeletor.aplicarFiltroPeriodo(root, cb, predicates, dataInicial, dataFinal, atributo);

        assertEquals(1, predicates.size());
        assertEquals(pred, predicates.get(0));
        verify(cb).between(any(), eq(dataInicial), eq(dataFinal));
    }

    @Test
    void testAplicarFiltroPeriodo_ApenasDataInicial() {
        Root<?> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        List<Predicate> predicates = new ArrayList<>();

        LocalDate dataInicial = LocalDate.of(2023, 1, 1);
        LocalDate dataFinal = null;
        String atributo = "dataCriacao";

        when(root.get(atributo)).thenReturn(mock(Path.class));
        Predicate pred = mock(Predicate.class);
        when(cb.greaterThanOrEqualTo(any(), eq(dataInicial))).thenReturn(pred);

        BaseSeletor.aplicarFiltroPeriodo(root, cb, predicates, dataInicial, dataFinal, atributo);

        assertEquals(1, predicates.size());
        assertEquals(pred, predicates.get(0));
        verify(cb).greaterThanOrEqualTo(any(), eq(dataInicial));
    }

    @Test
    void testAplicarFiltroPeriodo_ApenasDataFinal() {
        Root<?> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        List<Predicate> predicates = new ArrayList<>();

        LocalDate dataInicial = null;
        LocalDate dataFinal = LocalDate.of(2023, 1, 31);
        String atributo = "dataCriacao";

        when(root.get(atributo)).thenReturn(mock(Path.class));
        Predicate pred = mock(Predicate.class);
        when(cb.lessThanOrEqualTo(any(), eq(dataFinal))).thenReturn(pred);

        BaseSeletor.aplicarFiltroPeriodo(root, cb, predicates, dataInicial, dataFinal, atributo);

        assertEquals(1, predicates.size());
        assertEquals(pred, predicates.get(0));
        verify(cb).lessThanOrEqualTo(any(), eq(dataFinal));
    }

    @Test
    void testAplicarFiltroPeriodo_DataInicialENull_DataFinalNull() {
        Root<?> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        List<Predicate> predicates = new ArrayList<>();

        // Nenhuma data
        BaseSeletor.aplicarFiltroPeriodo(root, cb, predicates, null, null, "dataCriacao");

        // Nenhuma condição adicionada
        assertTrue(predicates.isEmpty());
    }
}

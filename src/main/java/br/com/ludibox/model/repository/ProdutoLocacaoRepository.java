package br.com.ludibox.model.repository;

import br.com.ludibox.model.entity.Locacao;
import br.com.ludibox.model.entity.ProdutoLocacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoLocacaoRepository extends JpaRepository<ProdutoLocacao, Integer>, JpaSpecificationExecutor<ProdutoLocacao> {

    @Query("SELECT pl FROM ProdutoLocacao pl " +
            "JOIN pl.produto p " +
            "WHERE p.anunciante.id = :anuncianteId")
    List<ProdutoLocacao> findLocacoesRecebidas(@Param("anuncianteId") Integer anuncianteId);
}

package br.com.ludibox.model.repository;

import br.com.ludibox.model.entity.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findByAtivoTrue();

    List<Avaliacao> findByProdutoLocacaoProdutoIdAndAtivoTrue(Integer produtoId);

    @Query("SELECT AVG(a.estrelas) FROM Avaliacao a WHERE a.produtoLocacao.produto.id = :produtoId AND a.ativo = true")
    Double mediaPorProduto(@Param("produtoId") Integer produtoId);
}

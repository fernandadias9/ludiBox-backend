package br.com.ludibox.model.repository;

import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.entity.Produto;
import br.com.ludibox.model.enums.StatusProduto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer>, JpaSpecificationExecutor<Produto> {
    List<Produto> findByAnuncianteId(Integer pessoaId);

    @Query("SELECT p FROM Produto p WHERE p.anunciante.situacao = true")
    List<Produto> findByAnuncianteSituacaoTrue();



    @Query("SELECT p FROM Produto p WHERE p.status = :status AND p.anunciante.situacao = true")
    Page<Produto> findByStatusAndAnuncianteAtivo(@Param("status") StatusProduto status, Pageable pageable);

    @Query("SELECT p FROM Produto p WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%')) AND p.status = :status AND p.anunciante.situacao = true")
    Page<Produto> findByNomeContainingIgnoreCaseAndStatusAndAnuncianteAtivo(@Param("nome") String nome, @Param("status") StatusProduto status, Pageable pageable);

}

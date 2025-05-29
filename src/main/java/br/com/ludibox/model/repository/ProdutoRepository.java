package br.com.ludibox.model.repository;

import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.entity.Produto;
import br.com.ludibox.model.enums.StatusProduto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer>, JpaSpecificationExecutor<Produto> {
    List<Produto> findByAnuncianteId(Integer pessoaId);

    Page<Produto> findByStatus(StatusProduto status, Pageable pageable);

    Page<Produto> findByNomeContainingIgnoreCaseAndStatus(String nome, StatusProduto status, Pageable pageable);
}

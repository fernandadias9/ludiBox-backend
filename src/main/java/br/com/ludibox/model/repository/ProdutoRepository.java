package br.com.ludibox.model.repository;

import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer>, JpaSpecificationExecutor<Produto> {
    List<Produto> findByAnuncianteId(Integer pessoaId);
}

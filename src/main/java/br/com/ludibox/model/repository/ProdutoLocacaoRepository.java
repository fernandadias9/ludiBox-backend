package br.com.ludibox.model.repository;

import br.com.ludibox.model.entity.ProdutoLocacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoLocacaoRepository extends JpaRepository<ProdutoLocacao, Integer>, JpaSpecificationExecutor<ProdutoLocacao> {
}

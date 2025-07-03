package br.com.ludibox.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.ludibox.model.entity.Endereco;

import java.util.List;

@Repository
public interface EnderecoRepository extends 
	JpaRepository<Endereco, Integer>, JpaSpecificationExecutor<Endereco>{

	List<Endereco> findByPessoaId(Integer idPessoa);

	@Query("SELECT e FROM Endereco e WHERE e.pessoa.id = :pessoaId AND e.deletedAt IS NULL")
	List<Endereco> findByPessoaIdAndNotDeleted(@Param("pessoaId") Integer pessoaId);
}

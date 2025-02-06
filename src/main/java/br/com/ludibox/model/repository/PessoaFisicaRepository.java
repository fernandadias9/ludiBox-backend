package br.com.ludibox.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import br.com.ludibox.model.entity.PessoaFisica;

@Repository
	public interface PessoaFisicaRepository extends
		JpaRepository<PessoaFisica, Integer>, JpaSpecificationExecutor<PessoaFisica>{

	Optional<PessoaFisica> findByEmail(String email);
	
}

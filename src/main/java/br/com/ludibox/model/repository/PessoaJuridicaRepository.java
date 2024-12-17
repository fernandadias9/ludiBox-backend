package br.com.ludibox.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import br.com.ludibox.model.entity.PessoaJuridica;

@Repository
public interface PessoaJuridicaRepository extends
JpaRepository<PessoaJuridica, Integer>, JpaSpecificationExecutor<PessoaJuridica>{

	Optional<PessoaJuridica> findByEmail(String email);
	
}

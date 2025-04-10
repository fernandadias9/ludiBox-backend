package br.com.ludibox.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import br.com.ludibox.model.entity.Endereco;

@Repository
public interface EnderecoRepository extends 
	JpaRepository<Endereco, Integer>, JpaSpecificationExecutor<Endereco>{
	

}

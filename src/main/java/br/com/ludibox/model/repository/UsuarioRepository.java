package br.com.ludibox.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import br.com.ludibox.model.entity.Usuario;

@Repository
public interface UsuarioRepository extends
	JpaRepository<Usuario, Integer>, JpaSpecificationExecutor<Usuario>{

	Optional<Usuario> findByEmail(String email);
	
}

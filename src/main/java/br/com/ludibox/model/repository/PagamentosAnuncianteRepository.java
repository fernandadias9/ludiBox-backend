package br.com.ludibox.model.repository;

import br.com.ludibox.model.entity.PagamentosAnunciante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagamentosAnuncianteRepository extends JpaRepository<PagamentosAnunciante, Integer> {
}

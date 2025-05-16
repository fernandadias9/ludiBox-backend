package br.com.ludibox.model.repository;

import br.com.ludibox.model.entity.Cancelamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CancelamentoRepository extends JpaRepository<Cancelamento, Integer> {
}

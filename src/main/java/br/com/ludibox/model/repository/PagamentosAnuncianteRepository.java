package br.com.ludibox.model.repository;

import br.com.ludibox.model.entity.PagamentosAnunciante;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PagamentosAnuncianteRepository extends JpaRepository<PagamentosAnunciante, Integer> {

    List<PagamentosAnunciante> findByPago(boolean pago);
}

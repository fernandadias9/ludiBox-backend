package br.com.ludibox.model.repository;

import br.com.ludibox.model.entity.Locacao;
import br.com.ludibox.model.entity.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocacaoRepository extends JpaRepository<Locacao, Integer>, JpaSpecificationExecutor<Locacao> {
    @Query("SELECT l FROM Locacao l WHERE l.locador.id = :usuarioId AND l.status = 'PENDENTE'")
    Optional<Locacao> findByUsuarioIdAndStatusPendente(@Param("usuarioId") Integer usuarioId);
}

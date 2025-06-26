package br.com.ludibox.model.repository;

import br.com.ludibox.model.entity.Locacao;
import br.com.ludibox.model.entity.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocacaoRepository extends JpaRepository<Locacao, Integer>, JpaSpecificationExecutor<Locacao> {
    @Query("SELECT l FROM Locacao l WHERE l.locador.id = :usuarioId AND l.status = 'PENDENTE'")
    Optional<Locacao> findByUsuarioIdAndStatusPendente(@Param("usuarioId") Integer usuarioId);

    @Query("SELECT l FROM Locacao l WHERE l.locador.id = :usuarioId")
    List<Locacao> findLocacoesEfetuadas(@Param("usuarioId") Integer usuarioId);

    @Query("SELECT COUNT(l) FROM Locacao l WHERE l.dataHoraPagamento BETWEEN :inicio AND :fim")
    long contarLocacoesNoMesAtual(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT COALESCE(SUM(l.valorTotal),0) FROM Locacao l WHERE l.dataHoraPagamento BETWEEN :inicio AND :fim")
    double somarValorBruto( @Param("inicio") LocalDateTime inicio, @Param("fim")    LocalDateTime fim);
}

package br.com.ludibox.model.repository;

import br.com.ludibox.model.enums.EnumStatusDenuncia;
import br.com.ludibox.model.enums.EnumMotivoDenuncia;
import br.com.ludibox.model.entity.Denuncia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DenunciaRepository extends JpaRepository<Denuncia, Integer> {

    @Query("SELECT d FROM Denuncia d " +
            "WHERE (:motivo IS NULL OR d.motivo = :motivo) " +
            "AND (:status IS NULL OR d.status = :status) " +
            "AND (:dataInicio IS NULL OR d.dataCriacao >= :dataInicio) " +
            "AND (:dataFim IS NULL OR d.dataCriacao <= :dataFim)")
    List<Denuncia> buscarComFiltros(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            @Param("motivo") EnumMotivoDenuncia motivo,
            @Param("status") EnumStatusDenuncia status
    );

    @Query("SELECT COUNT(d) FROM Denuncia d WHERE d.dataCriacao BETWEEN :inicio AND :fim")
    long contarDenunciasNoMesAtual(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}

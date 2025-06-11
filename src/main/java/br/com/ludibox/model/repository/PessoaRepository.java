package br.com.ludibox.model.repository;

import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.enums.EnumStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PessoaRepository extends
        JpaRepository<Pessoa, Integer>, JpaSpecificationExecutor<Pessoa>{

    Optional<Pessoa> findByEmail(String email);
    Optional<Pessoa> findByValorDocumentoAndSituacao(String valorDocumento, boolean situacao);
    Optional<Pessoa> findByEmailAndSituacao(String email, boolean situacao);

    List<Pessoa> findAllByDataDesativacaoBeforeAndEmailNotLike(LocalDateTime data, String pattern);

    @Modifying
    @Transactional
    @Query("UPDATE Pessoa p SET p.email = :email, p.valorDocumento = :documento WHERE p.id = :id")
    void anonimizarDados(@Param("id") Integer id,
                         @Param("email") String email,
                         @Param("documento") String documento);


}

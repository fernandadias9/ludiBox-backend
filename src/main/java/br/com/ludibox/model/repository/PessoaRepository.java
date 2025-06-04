package br.com.ludibox.model.repository;

import br.com.ludibox.model.entity.Pessoa;
import br.com.ludibox.model.enums.EnumStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PessoaRepository extends
        JpaRepository<Pessoa, Integer>, JpaSpecificationExecutor<Pessoa>{

    Optional<Pessoa> findByEmail(String email);
    Optional<Pessoa> findByValorDocumentoAndSituacao(String valorDocumento, EnumStatus situacao);
    Optional<Pessoa> findByEmailAndSituacao(String email, EnumStatus situacao);
}

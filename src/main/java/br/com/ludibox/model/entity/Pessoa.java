package br.com.ludibox.model.entity;

import br.com.ludibox.model.interfaces.CnpjGroup;
import br.com.ludibox.model.interfaces.CpfGroup;
import br.com.ludibox.model.enums.EnumDocumento;
import br.com.ludibox.model.enums.EnumPerfil;
import br.com.ludibox.model.enums.EnumStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jdk.jfr.BooleanFlag;
import lombok.Data;
import lombok.NonNull;
import org.aspectj.lang.annotation.Before;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;
import org.hibernate.validator.group.GroupSequenceProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "pessoa")
@GroupSequenceProvider(PessoaGroupSequenceProvider.class)
public class Pessoa implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shared_seq")
    @SequenceGenerator(name = "shared_seq", sequenceName = "shared_seq", allocationSize = 1)
    private Integer id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100)
    private String nome;

    @Email
    @NotBlank(message = "Email é obrigatório")
    private String email;

    private String telefone;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 5, max = 500)
    private String senha;

    @BooleanFlag
    private boolean situacao;

    @Enumerated(EnumType.STRING)
    private EnumPerfil perfil;

    @NotNull(message = "Tipo de documento obrigatório")
    @Enumerated(EnumType.STRING)
    private EnumDocumento tipoDocumento;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(length = 10485760)
    private String imagemUsuarioEmBase64;

    @NotBlank(message = "Documento é obrigatório")
    @CPF(groups = CpfGroup.class)
    @CNPJ(groups = CnpjGroup.class)
    private String valorDocumento;

    @Column(name = "secret_totp", length = 100)
    private String secretTotp;


    @JsonBackReference
    @OneToMany(mappedBy = "pessoa")
    private List<Endereco> enderecos;

    @Column(name = "dataDesativacao")
    private LocalDateTime dataDesativacao;

    @Column(name = "dataCriacao")
    private LocalDateTime dataCriacao;

    @PrePersist
    protected void onCreate() {
        if (perfil == null) {
            perfil = EnumPerfil.USUARIO;
        }
        situacao = true;
        dataCriacao = LocalDateTime.now();
    }

    @Override
    public java.util.Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();

        list.add(new SimpleGrantedAuthority(perfil.toString()));

        return list;
    }


    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(this.situacao);
    }


}

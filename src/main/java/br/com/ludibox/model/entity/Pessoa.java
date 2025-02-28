package br.com.ludibox.model.entity;

import br.com.ludibox.model.enums.EnumDocumento;
import br.com.ludibox.model.enums.EnumPerfil;
import br.com.ludibox.model.enums.EnumStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jdk.jfr.Enabled;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "pessoa")
public class Pessoa implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shared_seq")
    @SequenceGenerator(name = "shared_seq", sequenceName = "shared_seq", allocationSize = 1)
    private Integer id;

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @Email
    @NotBlank(message = "Email é obrigatório")
    private String email;

    private String telefone;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 5, max = 500)
    private String senha;

    @Enumerated(EnumType.STRING)
    private EnumStatus situacao;

    @Enumerated(EnumType.STRING)
    private EnumPerfil perfil;

    @Enumerated(EnumType.STRING)
    private EnumDocumento tipoDocumento;

    @Column(columnDefinition = "TEXT")
    private String imagemUsuarioEmBase64;

    @NotBlank(message = "Documento é obrigatório")
    private String valorDocumento;

    @JsonBackReference
    @OneToMany(mappedBy = "pessoa")
    private List<Endereco> enderecos;

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




}

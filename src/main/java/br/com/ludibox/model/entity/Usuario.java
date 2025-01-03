package br.com.ludibox.model.entity;

import br.com.ludibox.model.enums.EnumPerfil;
import br.com.ludibox.model.enums.EnumStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@MappedSuperclass
public abstract class Usuario implements UserDetails{

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shared_seq")
	@SequenceGenerator(name = "shared_seq", sequenceName = "shared_seq", allocationSize = 1)
	private Integer id;

	@Email
	@NotBlank(message = "Email é obrigatório")
	private String email;

	private String telefone;

	@NotBlank(message = "Senha é obrigatória")
	@Size(min = 5)
	@Column(length = 4000)
	private String senha;

	@Enumerated(EnumType.STRING)
	private EnumStatus situacao;

	@Enumerated(EnumType.STRING)
	private EnumPerfil perfil;
	
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


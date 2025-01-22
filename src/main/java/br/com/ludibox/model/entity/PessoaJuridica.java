package br.com.ludibox.model.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.br.CNPJ;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "pessoa_juridica")
public class PessoaJuridica extends Usuario{

	@NotBlank(message = "Razão social obrigatória")
	@Size(min = 3)
	private String razaoSocial;
	
	@CNPJ
	@NotBlank(message = "CNPJ é obrigatório")
	private String cnpj;
	
	@JsonBackReference
	@OneToOne(mappedBy = "pessoaJuridica")
    private Endereco endereco;
	
	@Override
	public java.util.Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
		
		list.add(new SimpleGrantedAuthority(super.getPerfil().toString()));
		
		return list;
	}


	@Override
	public String getPassword() {
		return super.getSenha();
	}


	@Override
	public String getUsername() {
		return this.getEmail();
	
	}
}

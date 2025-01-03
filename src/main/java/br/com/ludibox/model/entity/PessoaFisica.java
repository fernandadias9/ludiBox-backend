package br.com.ludibox.model.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "pessoa_fisica")
public class PessoaFisica extends Usuario{

	@NotBlank(message = "Nome é obrigatório")
	@Size(min = 3)
	private String nome;
	
	private LocalDate dataNascimento;
	
	@CPF
	@NotBlank(message = "CPF é obrigatório")
	private String cpf;
	
	@JsonBackReference
	@OneToMany(mappedBy = "pessoaFisica")
    private List<Endereco> enderecos;
	
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

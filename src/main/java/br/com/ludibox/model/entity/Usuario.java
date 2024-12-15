package br.com.ludibox.model.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import br.com.ludibox.model.enums.EnumPerfil;
import br.com.ludibox.model.enums.EnumStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table
public class Usuario {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private String email;
	
	private String telefone;
	
	private String senha;
	
	@JsonBackReference
	@OneToMany(mappedBy = "usuario")
	private List<Endereco> enderecos;
	
	@Enumerated(EnumType.STRING)
	private EnumStatus situacao;
	
	@Enumerated(EnumType.STRING)
	private EnumPerfil perfil;
	
	@PrePersist
		protected void onCreate() {
			situacao = situacao.ATIVO;
	}
	
	
	

}

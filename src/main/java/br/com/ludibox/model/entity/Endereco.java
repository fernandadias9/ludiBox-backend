package br.com.ludibox.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
@Table
public class Endereco {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "id_usuario")
	private Usuario usuario;
	
	private String nomeEndereco;
	
	@NotNull(message = "CEP é obrigatório")
	private Integer cep;
	
	private int numero;
	
	private String rua;
	
	private String complemento;
	
	private String cidade;
	
	private String estado;
	
	private String referencia;

}

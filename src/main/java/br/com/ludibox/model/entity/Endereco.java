package br.com.ludibox.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table
public class Endereco {
		
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Integer id;
		
		@NotBlank(message = "Nome do endereço é obrigatório")
		@Size(min = 3)
		private String nome;
		
		@NotNull(message = "CEP é obrigatório")
		private Integer cep;
		
		@NotBlank(message = "Rua é obrigatória")
		@Size(min = 3)
		private String rua;
		
		@NotNull(message = "Número é obrigatório")
		private Integer numero;
		
		@NotNull(message = "Campo obrigatório")
		private boolean semNumero;
		
		private String complemento;
		
		@NotBlank(message = "Bairro é obrigatório")
		@Size(min = 3)
		private String bairro;
		
		@NotBlank(message = "Cidade é obrigatória")
		@Size(min = 3)
		private String cidade;
		
		@NotBlank(message = "Estado é obrigatório")
		@Size(min = 2)
		private String estado;
		
		@ManyToOne
	    @JoinColumn(name = "pessoa_id")
	    private Pessoa pessoa;
	}


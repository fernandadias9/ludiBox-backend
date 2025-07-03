package br.com.ludibox.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.Date;

@Data
@Entity
@Table
@SQLDelete(sql = "UPDATE endereco SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Endereco {
		
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Integer id;
		
		@NotBlank(message = "Nome do endereço é obrigatório")
		@Size(min = 3, max = 100)
		private String nome;
		
		@NotNull(message = "CEP é obrigatório")
		private Integer cep;
		
		@NotBlank(message = "Rua é obrigatória")
		@Size(min = 3)
		private String rua;

		private Integer numero;

		private Boolean semNumero;

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

		@Column(name = "deleted_at")
		@Temporal(TemporalType.TIMESTAMP)
		private Date deletedAt;

		public boolean isDeleted() {
			return deletedAt != null;
		}

		public void delete() {
			this.deletedAt = new Date();
		}
	}

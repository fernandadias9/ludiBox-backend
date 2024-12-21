package br.com.ludibox.model.entity;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.validator.constraints.br.CNPJ;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
@Table
public class PessoaJuridica extends Usuario{
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@NotBlank(message = "Razão social obrigatória")
	@Size(min = 3)
	private String razaoSocial;
	
	@CNPJ
	@NotBlank(message = "CNPJ é obrigatório")
	private String cnpj;

}

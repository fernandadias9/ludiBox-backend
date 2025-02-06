	package br.com.ludibox.model.seletor;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public abstract class BaseSeletor {
		
		private int pagina;
		private int limite;
	
		public BaseSeletor() {
			this.limite = 0;
			this.pagina = 0;
		}
		
		public boolean temPaginacao() {
			return this.limite > 0 && this.pagina > 0;	
		}
		
		public boolean stringValida(String texto) {
			return texto != null && !texto.isBlank();
		}
		
		public int getOffset() {
			return this.limite * (this.pagina - 1);
		}
		
		public int getPagina() {
			return pagina;
		}
	
		public void setPagina(int pagina) {
			this.pagina = pagina;
		}
	
		public int getLimite() {
			return limite;
		}
	
		public void setLimite(int limite) {
			this.limite = limite;
		}
		
		public static void aplicarFiltroPeriodo(Root root, CriteriaBuilder cb, List<Predicate> predicates,
				LocalDate dataInicial, LocalDate dataFinal, String nomeAtributo) {
			if(dataInicial != null && dataFinal != null) {
				predicates.add(cb.between(root.get(nomeAtributo), dataInicial, dataFinal));
			}else if(dataInicial != null){
				predicates.add(cb.greaterThanOrEqualTo(root.get(nomeAtributo), dataInicial));
			}else if(dataFinal != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get(nomeAtributo), dataFinal));
			}
		}
		
	}

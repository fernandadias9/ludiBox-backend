package br.com.ludibox.model.entity;

import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

import java.util.ArrayList;
import java.util.List;

public class PessoaGroupSequenceProvider implements DefaultGroupSequenceProvider<Pessoa> {

    @Override
    public List<Class<?>> getValidationGroups(Pessoa pessoa) {
        List<Class<?>> groups = new ArrayList<>();
        groups.add(Pessoa.class);
        if (isPessoaSelecionada(pessoa)){
            groups.add(pessoa.getTipoDocumento().getGroup());
        }
        return groups;
    }
    protected boolean isPessoaSelecionada(Pessoa pessoa){
        return pessoa != null && pessoa.getTipoDocumento() != null;
    }
}

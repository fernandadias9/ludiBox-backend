package br.com.ludibox.model.enums;

import br.com.ludibox.model.interfaces.CnpjGroup;
import br.com.ludibox.model.interfaces.CpfGroup;

public enum EnumDocumento {

    CNPJ(CnpjGroup.class),
    CPF(CpfGroup.class),
    ;

    private final Class<?> group;

    private EnumDocumento(Class<?> group){
        this.group = group;
    }

    public Class<?> getGroup() {
        return group;
    }
}

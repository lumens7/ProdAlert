package br.com.lumens.DOMAIN;

import java.io.Serializable;
import java.util.Objects;

import lombok.*;

/*
Criado por Luís
*/

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VincularId implements Serializable {
    private String CNPJ;
    private String CPF;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VincularId that = (VincularId) o;
        return Objects.equals(CNPJ, that.CNPJ) &&
               Objects.equals(CPF, that.CPF);
    }

    @Override
    public int hashCode() {
        return Objects.hash(CNPJ, CPF);
    }
}

package br.com.lumens.DOMAIN;

import jakarta.persistence.*;
import lombok.*;

/*
Criado por Luís
*/

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbvincular")
@IdClass(VincularId.class)
public class Vincular {

    @Id
    @Column(name = "CNPJ", nullable = false, length = 14)
    private String CNPJ;

    @Id
    @Column(name = "CPF", nullable = false, length = 11)
    private String CPF;
}

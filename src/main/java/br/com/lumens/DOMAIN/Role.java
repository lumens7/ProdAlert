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
@Table(name = "tbrole")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRole;

    private String nomeRole;
}
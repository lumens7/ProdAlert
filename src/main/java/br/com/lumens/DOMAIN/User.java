package br.com.lumens.DOMAIN;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/*
Criado por Luís
*/

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbuser")
public class User {

    public enum StatusUser {
        ATIVO, INATIVO;
    }

    public enum functionRole {
        EMPRESA, FUNCIONARIO;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUser;

    @NotBlank(message = "O nome do usuário é obrigatório")
    @Column(name = "nomeUser", nullable = false)
    private String nomeUser;

    @Size(min = 11, max = 11, message = "O CPF deve conter exatamente 11 algarismos!")
    @Column(name = "CPF", nullable = true, unique = true, length = 11)
    private String CPF;

    @Size(min = 14, max = 14, message = "O CNPJ deve conter exatamente 14 algarismos!")
    @Column(name = "CNPJ", nullable = true, unique = true, length = 14)
    private String CNPJ;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "O e-mail deve ser válido")
    @Column(name = "mail", nullable = false)
    private String mail;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    @Column(name = "senha", nullable = false, length = 255)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "statusUser")
    private StatusUser statusUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "funcao")
    private functionRole function;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuarioRole", 
        joinColumns = @JoinColumn(name = "idUser"), 
        inverseJoinColumns = @JoinColumn(name = "idRole")
    )
    private Set<Role> roles = new HashSet<>();
    
} 
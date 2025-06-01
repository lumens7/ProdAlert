package br.com.lumens.DOMAIN;

import java.math.BigDecimal;
import java.time.LocalDate;

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
@Table(name = "tbproduto")  
public class Produto {
    
    public enum Status {
        ATIVO, INATIVO;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProduto;

    @Column(name = "nomeProduto", nullable = false)
    private String nomeProduto;

    @Column(name = "descricaoProduto", nullable = false)
    private String descricaoProduto;

    @Column(name = "codBarras", nullable = true, length = 13)
    private String codBarras;

    @Column(name = "dataContagem", nullable = true)
    private LocalDate dataContagem;

    @Column(name = "dataValidade", nullable = false)
    private LocalDate dataValidade;

    @Column(name = "quantidadeContada", nullable = false)
    private BigDecimal quantidadeContada;

    @Enumerated(EnumType.STRING)
    @Column(name = "statusProduto")
    private Status status;
    
    @ManyToOne
    @JoinColumn(name = "idFuncionario", nullable = false)
    private User funcionario;
}
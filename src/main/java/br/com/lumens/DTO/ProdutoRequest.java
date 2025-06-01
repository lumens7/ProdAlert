package br.com.lumens.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
Criado por Luís
*/

@Getter
@Setter
@NoArgsConstructor
public class ProdutoRequest {

    private String nomeProduto;
    private String descricaoProduto;
    private String codBarras;
    private String dataValidade; 
    private BigDecimal quantidadeContada;

    public LocalDate getDataValidadeAsLocalDate() {
        if (this.dataValidade == null || this.dataValidade.trim().isEmpty()) {
            throw new IllegalArgumentException("A data de validade não pode ser nula ou vazia.");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            return LocalDate.parse(this.dataValidade, formatter);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de data inválido. Use o padrão dd/MM/yyyy.");
        }
    }
}
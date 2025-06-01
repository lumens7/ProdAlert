package br.com.lumens.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
Criado por Luís
*/

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmacaoSenhaRequest {
    private String mail;
    private String codigo;
    private String novaSenha;
    private String ConfirmacaoSenha;
}
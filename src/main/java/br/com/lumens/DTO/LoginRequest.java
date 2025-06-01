package br.com.lumens.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
Criado por Luís
*/

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {
	private String mail;
    private String senha;
}

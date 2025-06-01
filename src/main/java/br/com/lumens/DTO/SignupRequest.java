package br.com.lumens.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
Criado por Luís
*/

@Getter
@Setter
@NoArgsConstructor
public class SignupRequest {
    
    @NotBlank(message = "O nome é obrigatório")
    private String nomeUser;
    
    private String Cpfcnpj;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "O e-mail deve ser válido")
    private String mail;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, message = "A senha deve ter exatamente 8 caracteres")
    private String senha;
    
    @NotBlank(message = "Deve ser enviado a comparação da senha.")
    private String confirmarSenha;
    
}
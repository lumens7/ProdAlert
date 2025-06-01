package br.com.lumens.DTO;

import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotBlank;
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
@Component
public class CompleteSignupRequest {
	@NotBlank(message = "E-mail é obrigatório")
    private String email;
    
    @NotBlank(message = "Código de verificação é obrigatório")
    private String code;

    
}

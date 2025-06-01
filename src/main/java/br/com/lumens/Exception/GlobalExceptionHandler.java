package br.com.lumens.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

/*
Criado por Luís
*/

@ControllerAdvice
public class GlobalExceptionHandler {

 	@ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<String> handleInvalidFormatException(InvalidFormatException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Formato de dados inválido. Verifique os valores enviados: " + ex.getMessage());
    }
}
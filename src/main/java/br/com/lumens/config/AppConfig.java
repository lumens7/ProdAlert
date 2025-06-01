package br.com.lumens.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/*
 Criado por Luís
 */

@Configuration
public class AppConfig {

    // Cria e fornece um bean do RestTemplate para chamadas HTTP
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

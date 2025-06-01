package br.com.lumens;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/*
Criado por Luís
*/

@SpringBootApplication  
@EnableScheduling 	
@ComponentScan(basePackages = {"br.com.lumens"})
public class ProdAlertApplication extends SpringBootServletInitializer {

	 public static void main(String[] args) {
	        SpringApplication.run(ProdAlertApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ProdAlertApplication.class);
    }

}

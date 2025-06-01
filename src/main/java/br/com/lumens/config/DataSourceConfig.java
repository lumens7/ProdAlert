package br.com.lumens.config;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
Criado por Luís
*/

public class DataSourceConfig {
    @Bean
    public DataSource getDataSource() {
        return DataSourceBuilder.create()
          .driverClassName("${database}")
          .url("${db_url}")
          .username("${db_username}")
          .password("${db_password}")
          .build();	
    }
}
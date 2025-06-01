package br.com.lumens.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;


/*
Criado por Luís
*/

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("tempSignupData") {
            @Override
            protected org.springframework.cache.concurrent.ConcurrentMapCache createConcurrentMapCache(String name) {
                return new org.springframework.cache.concurrent.ConcurrentMapCache(
                    name,
                    false 
                );
            }
        };
    }
}

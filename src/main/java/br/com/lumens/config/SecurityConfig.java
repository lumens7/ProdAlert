package br.com.lumens.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import br.com.lumens.jwtSecurity.JwtAuthenticationFilter;
import br.com.lumens.jwtSecurity.JwtAuthorizationFilter;

import java.util.List;

/*
Criado por Luís
*/

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${secretkey}")
    private String secretKey;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) 
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/usuario/init-signup", "/api/usuario/login", "/api/usuario/complete-signup", "/api/usuario/verify-code", "/api/usuario/alterarSenha", "/api/usuario/confirmarAlteracaoSenha").permitAll()
                .requestMatchers("/api/produtos/**").hasAnyRole("FUNCIONARIO", "EMPRESA", "ADMIN")
                .requestMatchers("/api/usuario/buscar/email").hasAnyRole("FUNCIONARIO", "EMPRESA", "ADMIN")
                .requestMatchers("/api/usuario/dados_usuario").hasAnyRole("FUNCIONARIO", "EMPRESA", "ADMIN")
                .requestMatchers("/api/usuario/dados_empresa").hasAnyRole("EMPRESA", "ADMIN")
                .requestMatchers("/api/usuario/**").hasAnyRole("EMPRESA", "ADMIN")
                .requestMatchers("/api/role/**").hasAuthority("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(authenticationManager, secretKey), UsernamePasswordAuthenticationFilter.class)
            .addFilter(new JwtAuthorizationFilter(authenticationManager, secretKey))
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
        		"http://localhost:3000",
                "http://192.168.1.5",
                "http://localhost:80",
                "http://localhost:8080",
                "http://127.0.0.1:5500"
        ));
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Access-Control-Allow-Origin"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}


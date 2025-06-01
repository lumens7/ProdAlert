package br.com.lumens.jwtSecurity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.lumens.DTO.LoginRequest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Base64;

/*
Criado por Luís
*/

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final Key secretKey;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, String secret) {
        this.authenticationManager = authenticationManager;
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        setFilterProcessesUrl("/api/usuario/login"); 
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            String requestBody = request.getReader().lines().collect(Collectors.joining());
            LoginRequest creds = new ObjectMapper().readValue(requestBody, LoginRequest.class);
            return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(creds.getMail(), creds.getSenha())
            );
        } catch (IOException e) {
            throw new RuntimeException("Falha ao processar login", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
        UserDetails userDetails = (UserDetails) authResult.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        String token = Jwts.builder()
            .setSubject(userDetails.getUsername())
            .claim("roles", roles)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 14400000)) // 4 horas
            .signWith(secretKey, SignatureAlgorithm.HS512)
            .compact();

        response.addHeader("Authorization", "Bearer " + token);
        response.getWriter().write("{\"token\": \"Bearer " + token + "\"}");
        response.getWriter().flush();
    }
}

package br.com.lumens.jwtSecurity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.ExpiredJwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/*
Criado por Luís
*/

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final Key secretKey;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, String secret) {
        super(authenticationManager);
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.replace("Bearer ", "").trim();
        UsernamePasswordAuthenticationToken authentication = getAuthentication(token);

        if (authentication != null) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(String token) {
        if (token != null) {
            try {
                Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey) 
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

                String user = claims.getSubject();
                List<String> roles = claims.get("roles", List.class);

                if (user != null) {
                    return new UsernamePasswordAuthenticationToken(user, null, roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()));
                }
            } catch (ExpiredJwtException e) {
                System.out.println("Token expirado: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Token inválido: " + e.getMessage());
            }
        }
        return null;
    }
}

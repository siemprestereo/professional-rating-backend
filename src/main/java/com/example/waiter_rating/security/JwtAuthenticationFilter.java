package com.example.waiter_rating.security;

import com.example.waiter_rating.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                Claims claims = jwtService.validateToken(token);
                Long userId = claims.get("userId", Long.class);
                String userType = claims.get("userType", String.class);
                String email = claims.get("email", String.class);

                // Crear autenticación
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Guardar en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Guardar userId y userType en atributos de la request para uso posterior
                request.setAttribute("userId", userId);
                request.setAttribute("userType", userType);

                System.out.println("✅ JWT validado - User: " + email + " (ID: " + userId + ", Type: " + userType + ")");
            }

        } catch (Exception e) {
            System.out.println("❌ Error validando JWT: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
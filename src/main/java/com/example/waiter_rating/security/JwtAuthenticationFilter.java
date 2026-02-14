package com.example.waiter_rating.security;

import com.example.waiter_rating.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Si no hay header o no empieza con Bearer, seguimos sin autenticar (Spring decidirá si deja pasar o no)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);

            // Validamos el token. Si está expirado o es falso, JwtService debería lanzar una excepción.
            Claims claims = jwtService.validateToken(token);

            Long userId = claims.get("userId", Long.class);
            String userType = claims.get("userType", String.class);
            String email = claims.get("email", String.class);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Establecemos la identidad del usuario en el contexto
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Guardamos datos útiles en la request para los Controllers
                request.setAttribute("userId", userId);
                request.setAttribute("userType", userType);

                log.debug("JWT validado para usuario: {}", email);
            }

        } catch (JwtException | IllegalArgumentException e) {
            // Si el token falla, limpiamos cualquier intento de autenticación previo por seguridad
            SecurityContextHolder.clearContext();
            log.warn("Intento de acceso con JWT inválido: {}", e.getMessage());

            // Opcional: Podrías enviar un 401 aquí mismo, pero es mejor dejar que
            // la configuración de SecurityConfig maneje el error según el endpoint.
        }

        filterChain.doFilter(request, response);
    }
}
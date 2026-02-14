package com.example.waiter_rating.service.impl;

import com.example.waiter_rating.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException; // Importante
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtServiceImpl implements JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtServiceImpl.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        // Usamos StandardCharsets.UTF_8 para evitar problemas de encoding entre sistemas
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateToken(Long userId, String userType, String email, String name) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);
        claims.put("email", email);
        claims.put("name", name);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            // Logueamos internamente pero lanzamos una excepción de seguridad específica
            log.warn("Fallo en la validación del token: {}", e.getMessage());
            throw new JwtException("Token inválido, expirado o malformado");
        }
    }

    @Override
    public Long getUserIdFromToken(String token) {
        return validateToken(token).get("userId", Long.class);
    }

    @Override
    public String getUserTypeFromToken(String token) {
        return validateToken(token).get("userType", String.class);
    }
}
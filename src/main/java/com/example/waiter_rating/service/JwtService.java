package com.example.waiter_rating.service;

import io.jsonwebtoken.Claims;

public interface JwtService {

    String generateToken(Long userId, String userType, String email, String name);

    Claims validateToken(String token);

    Long getUserIdFromToken(String token);

    String getUserTypeFromToken(String token);
}
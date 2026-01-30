package com.example.waiter_rating.service.impl;

import com.example.waiter_rating.dto.response.AppUserResponse;
import com.example.waiter_rating.model.AppUser;
import com.example.waiter_rating.repository.AppUserRepo;
import com.example.waiter_rating.service.AppUserService;
import com.example.waiter_rating.service.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepo repo;
    private final JwtService jwtService;

    @Autowired
    public AppUserServiceImpl(AppUserRepo repo, JwtService jwtService) {
        this.repo = repo;
        this.jwtService = jwtService;
    }

    @Override
    public AppUserResponse getById(Long id) {
        AppUser user = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        return mapToResponse(user);
    }

    @Override
    public List<AppUserResponse> listAll() {
        List<AppUser> users = repo.findAll();
        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> checkUserRoles(String authHeader) {
        try {
            System.out.println("=== checkUserRoles START ===");

            String token = authHeader.substring(7);
            Claims claims = jwtService.validateToken(token);
            String email = claims.getSubject();
            System.out.println("Email del token: " + email);

            AppUser user = repo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Con AppUser unificado, un usuario puede tener AMBOS roles configurados
            // Solo necesitamos verificar si tiene los campos de Professional poblados
            boolean hasClientRole = true; // Todos los usuarios pueden ser clientes
            boolean hasProfessionalRole = user.getProfessionType() != null; // Solo si tiene profession type configurado

            System.out.println("hasClientRole: " + hasClientRole);
            System.out.println("hasProfessionalRole: " + hasProfessionalRole);
            System.out.println("activeRole: " + user.getActiveRole());

            Map<String, Object> response = new HashMap<>();
            response.put("hasClientRole", hasClientRole);
            response.put("hasProfessionalRole", hasProfessionalRole);
            response.put("activeRole", user.getActiveRole().name());
            response.put("canSwitchRole", user.canSwitchRole());

            if (!user.canSwitchRole()) {
                response.put("nextAllowedSwitchDate", user.getNextAllowedRoleSwitchDate());
            }

            System.out.println("Response: " + response);
            System.out.println("=== checkUserRoles SUCCESS ===");
            return response;
        } catch (Exception e) {
            System.err.println("=== checkUserRoles ERROR ===");
            e.printStackTrace();
            throw new RuntimeException("Error verificando roles: " + e.getMessage());
        }
    }

    private AppUserResponse mapToResponse(AppUser user) {
        AppUserResponse response = new AppUserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setLocation(user.getLocation());
        response.setProfessionalTitle(user.getProfessionalTitle());
        response.setUserType(user.getUserType());
        response.setActiveRole(user.getActiveRole().name());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
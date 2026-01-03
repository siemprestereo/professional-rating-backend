package com.example.waiter_rating.dto.request;

import com.example.waiter_rating.model.AppUser;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SwitchRoleRequest {

    @NotNull(message = "El nuevo rol es requerido")
    private AppUser.UserRole newRole;

    // Solo requerido si newRole es PROFESSIONAL y el usuario no tiene perfil profesional aún
    private String professionType; // Ej: "WAITER", "ELECTRICIAN", "PAINTER"

    private String professionalTitle; // Ej: "Mozo Profesional", "Electricista Matriculado"
}
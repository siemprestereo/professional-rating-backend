package com.example.waiter_rating.service;

import com.example.waiter_rating.model.AppUser;

public interface RoleSwitchService {

    /**
     * Cambia el rol activo del usuario
     * @param userId ID del usuario
     * @param newRole Nuevo rol (CLIENT o PROFESSIONAL)
     * @param professionType Solo requerido si newRole es PROFESSIONAL y el usuario no tiene perfil profesional
     * @param professionalTitle Título profesional (opcional)
     * @return El usuario actualizado
     */
    AppUser switchRole(Long userId, AppUser.UserRole newRole, String professionType, String professionalTitle);
}
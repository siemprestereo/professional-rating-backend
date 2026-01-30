package com.example.waiter_rating.dto.request;

import com.example.waiter_rating.model.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SwitchRoleRequest {

    @NotNull(message = "El nuevo rol es requerido")
    private UserRole newRole;

    private String professionType;
    private String professionalTitle;
}

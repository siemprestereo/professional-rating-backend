package com.example.waiter_rating.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PhotoConfirmRequest {

    @NotBlank(message = "El public_id es requerido")
    private String publicId;
}
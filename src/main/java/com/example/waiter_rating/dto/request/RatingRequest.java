package com.example.waiter_rating.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RatingRequest {

    @NotNull(message = "El puntaje es obligatorio")
    @Min(value = 1, message = "El puntaje mínimo es 1")
    @Max(value = 5, message = "El puntaje máximo es 5")
    private Integer score;

    @Size(max = 500, message = "El comentario no puede superar 500 caracteres")
    private String comment;

    // Para flujo SIN QR: requeridos
    @NotNull(message = "professionalId es obligatorio cuando no se usa QR")
    private Long professionalId;

    // businessId es OPCIONAL (se usa workHistoryId en su lugar)
    private Long businessId;

    // Lugar de trabajo específico - REQUERIDO si el profesional tiene múltiples trabajos activos
    private Long workHistoryId;

    // clientId viene del usuario autenticado
    private Long clientId;
}
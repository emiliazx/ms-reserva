package com.costuras.disponibilidad.dto;




import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearReservaRequest {

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "No se pueden agendar citas en el pasado")
    private LocalDate fecha;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    // Descripción o motivo de la cita 
    private String descripcion;

    @NotBlank(message = "El email del cliente es obligatorio")
    private String emailCliente;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String nombreCliente;
}

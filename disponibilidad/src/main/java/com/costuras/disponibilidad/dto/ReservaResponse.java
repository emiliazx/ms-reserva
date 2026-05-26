package com.costuras.disponibilidad.dto;

import com.costuras.disponibilidad.model.EstadoReserva;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaResponse {
    private Integer id;
    private Integer idCliente;
    private String nombreCliente;
    private String emailCliente;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String descripcion;
    private EstadoReserva estado;
    private LocalDateTime creadoEn;
    private String motivoCancelacion;
}

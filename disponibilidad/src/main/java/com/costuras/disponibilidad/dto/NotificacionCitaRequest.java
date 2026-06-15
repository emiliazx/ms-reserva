package com.costuras.disponibilidad.dto;



import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;




@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionCitaRequest {

    private String idReserva;
    private Integer idCliente;
    private String emailCliente;
    private String nombreCliente;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String descripcion;

            
    private String tipoNotificacion;
    private String motivoCancelacion;
}

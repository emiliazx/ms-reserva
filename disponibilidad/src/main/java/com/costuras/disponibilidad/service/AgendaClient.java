package com.costuras.disponibilidad.service;

import java.time.LocalDate;
import java.time.LocalTime;

public interface AgendaClient {

   

    boolean esSlotValido(LocalDate fecha, LocalTime horaInicio);

 

    int obtenerDuracionSlot(LocalDate fecha, LocalTime horaInicio);
}

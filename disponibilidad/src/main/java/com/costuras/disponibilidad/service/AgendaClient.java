package com.costuras.disponibilidad.service;

import java.time.LocalDate;
import java.time.LocalTime;

public interface AgendaClient {

   // Valida disponibilidad del slot contra configuraciones, festivos y bloqueos

    boolean esSlotValido(LocalDate fecha, LocalTime horaInicio);

  // Retorna la duración del slot en minutos

    int obtenerDuracionSlot(LocalDate fecha, LocalTime horaInicio);
}

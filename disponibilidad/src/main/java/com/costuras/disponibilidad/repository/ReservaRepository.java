package com.costuras.disponibilidad.repository;

import com.costuras.disponibilidad.model.EstadoReserva;
import com.costuras.disponibilidad.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

   // Reservas de un cliente 
    List<Reserva> findByIdClienteOrderByFechaDescHoraInicioDesc(Integer idCliente);

    //Reservas activas en una fecha (para admin) 
    List<Reserva> findByFechaAndEstadoNotOrderByHoraInicioAsc(LocalDate fecha, EstadoReserva estado);

    //¿Ya existe una reserva activa en ese slot? 
    boolean existsByFechaAndHoraInicioAndEstadoNot(LocalDate fecha, LocalTime horaInicio, EstadoReserva estado);

    
     //Horas de inicio ocupadas para una fecha (reservas activas).
     //Lo usa MS-Agenda para calcular slots libres.
    
    @Query("""
        SELECT r.horaInicio FROM Reserva r
        WHERE r.fecha = :fecha
          AND r.estado <> 'CANCELADA'""")List<LocalTime> findHorasOcupadasByFecha(@Param("fecha") LocalDate fecha);

    // Todas las reservas en un rango de fechas (admin)
    List<Reserva> findByFechaBetweenOrderByFechaAscHoraInicioAsc(LocalDate desde, LocalDate hasta);
}

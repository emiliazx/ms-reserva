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

  
    List<Reserva> findByIdClienteOrderByFechaDescHoraInicioDesc(Integer idCliente);

     
    List<Reserva> findByFechaAndEstadoNotOrderByHoraInicioAsc(LocalDate fecha, EstadoReserva estado);

    
    boolean existsByFechaAndHoraInicioAndEstadoNot(LocalDate fecha, LocalTime horaInicio, EstadoReserva estado);

    
   
    @Query("""
        SELECT r.horaInicio FROM Reserva r
        WHERE r.fecha = :fecha
          AND r.estado <> 'CANCELADA'""")List<LocalTime> findHorasOcupadasByFecha(@Param("fecha") LocalDate fecha);

    
    List<Reserva> findByFechaBetweenOrderByFechaAscHoraInicioAsc(LocalDate desde, LocalDate hasta);
}

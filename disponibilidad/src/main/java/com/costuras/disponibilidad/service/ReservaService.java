package com.costuras.disponibilidad.service;



import com.costuras.disponibilidad.dto.CancelarReservaRequest;
import com.costuras.disponibilidad.dto.CrearReservaRequest;
import com.costuras.disponibilidad.dto.NotificacionCitaRequest;
import com.costuras.disponibilidad.dto.ReservaResponse;
import com.costuras.disponibilidad.model.EstadoReserva;
import com.costuras.disponibilidad.model.Reserva;
import com.costuras.disponibilidad.repository.ReservaRepository;
import com.costuras.disponibilidad.security.UsuarioPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;


@Slf4j
@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepo;
    private final AgendaClient agendaClient;
    private final NotificacionesClient notificacionesClient;

   
     
    @Transactional
    public ReservaResponse crearReserva(CrearReservaRequest req, UsuarioPrincipal principal) {
        LocalDate fecha     = req.getFecha();
        LocalTime horaInicio = req.getHoraInicio();
            

        if (!agendaClient.esSlotValido(fecha, horaInicio)) {
            throw new IllegalArgumentException(
                "El slot " + horaInicio + " del " + fecha + " no está disponible en la agenda");
        }

        
        if (reservaRepo.existsByFechaAndHoraInicioAndEstadoNot(fecha, horaInicio, EstadoReserva.CANCELADA)) {
            throw new IllegalArgumentException(
                "El horario " + horaInicio + " del " + fecha + " ya está reservado");
        }

        
        int duracion = agendaClient.obtenerDuracionSlot(fecha, horaInicio);
        LocalTime horaFin = horaInicio.plusMinutes(duracion);

       
        Reserva reserva = Reserva.builder()
                .idCliente(principal.getId())
                .emailCliente(req.getEmailCliente())     
                .nombreCliente(req.getNombreCliente()) 
                .fecha(fecha)
                .horaInicio(horaInicio)
                .horaFin(horaFin)
                .descripcion(req.getDescripcion())
                .estado(EstadoReserva.CONFIRMADA)
                .build();

        reserva = reservaRepo.save(reserva);
        log.info("Reserva {} creada para cliente {} en {}-{}", reserva.getId(), principal.getId(), fecha, horaInicio);

       
        notificacionesClient.notificarCita(buildNotificacion(reserva, "AGENDADA"));

        return toResponse(reserva);
    }

 

    public List<ReservaResponse> misReservas(UsuarioPrincipal principal) {
        return reservaRepo.findByIdClienteOrderByFechaDescHoraInicioDesc(principal.getId())
                .stream().map(this::toResponse).toList();
    }

    public ReservaResponse obtenerReserva(Integer id, UsuarioPrincipal principal) {
        Reserva reserva = reservaRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Reserva no encontrada: " + id));
        if (!reserva.getIdCliente().equals(principal.getId()) && !"ADMIN".equals(principal.getRole())) {
            throw new SecurityException("No tienes permiso para ver esta reserva");
        }
        return toResponse(reserva);
    }

    

    
    @Transactional
    public ReservaResponse cancelarReserva(Integer id, CancelarReservaRequest req, UsuarioPrincipal principal) {
        Reserva reserva = reservaRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Reserva no encontrada: " + id));

        if (!reserva.getIdCliente().equals(principal.getId())) {
            throw new SecurityException("No tienes permiso para cancelar esta reserva");
        }
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new IllegalArgumentException("La reserva ya está cancelada");
        }
        if (reserva.getFecha().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se puede cancelar una reserva en el pasado");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva.setCanceladoEn(LocalDateTime.now());
        reserva.setCanceladoPor("CLIENTE");
        reserva.setMotivoCancelacion(req.getMotivo());
        reserva = reservaRepo.save(reserva);
        log.info("Reserva {} cancelada por cliente {}", id, principal.getId());

        notificacionesClient.notificarCita(buildNotificacion(reserva, "CANCELADA"));

        return toResponse(reserva);
    }


    @Transactional
    public ReservaResponse cancelarReservaAdmin(Integer id, CancelarReservaRequest req) {
        Reserva reserva = reservaRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Reserva no encontrada: " + id));
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new IllegalArgumentException("La reserva ya está cancelada");
        }
        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva.setCanceladoEn(LocalDateTime.now());
        reserva.setCanceladoPor("ADMIN");
        reserva.setMotivoCancelacion(req.getMotivo());
        reserva = reservaRepo.save(reserva);
        log.info("Reserva {} cancelada por ADMIN", id);

        notificacionesClient.notificarCita(buildNotificacion(reserva, "CANCELADA"));

        return toResponse(reserva);
    }

 

    public List<ReservaResponse> listarReservasPorFecha(LocalDate fecha) {
        return reservaRepo.findByFechaAndEstadoNotOrderByHoraInicioAsc(fecha, EstadoReserva.CANCELADA)
                .stream().map(this::toResponse).toList();
    }

    public List<ReservaResponse> listarReservasPorRango(LocalDate desde, LocalDate hasta) {
        return reservaRepo.findByFechaBetweenOrderByFechaAscHoraInicioAsc(desde, hasta)
                .stream().map(this::toResponse).toList();
    }

     
    public List<String> obtenerHorasOcupadas(LocalDate fecha) {
        return reservaRepo.findHorasOcupadasByFecha(fecha)
                .stream().map(LocalTime::toString).toList();
    }

   

    private ReservaResponse toResponse(Reserva r) {
        return ReservaResponse.builder()
                .id(r.getId())
                .idCliente(r.getIdCliente())
                .nombreCliente(r.getNombreCliente())
                .emailCliente(r.getEmailCliente())
                .fecha(r.getFecha())
                .horaInicio(r.getHoraInicio())
                .horaFin(r.getHoraFin())
                .descripcion(r.getDescripcion())
                .estado(r.getEstado())
                .creadoEn(r.getCreadoEn())
                .motivoCancelacion(r.getMotivoCancelacion())
                .build();
    }

    private NotificacionCitaRequest buildNotificacion(Reserva r, String tipo) {
        return NotificacionCitaRequest.builder()
                .idReserva(r.getId().toString())
                .idCliente(r.getIdCliente())
                .emailCliente(r.getEmailCliente())
                .nombreCliente(r.getNombreCliente())
                .fecha(r.getFecha())
                .horaInicio(r.getHoraInicio())
                .horaFin(r.getHoraFin())
                .descripcion(r.getDescripcion())
                .tipoNotificacion(tipo)
                .motivoCancelacion(r.getMotivoCancelacion())
                .build();
    }
}

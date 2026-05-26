package com.costuras.disponibilidad.controller;

import com.costuras.disponibilidad.dto.*;
import com.costuras.disponibilidad.security.UsuarioPrincipal;
import com.costuras.disponibilidad.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    //  CLIENTE 

    
      //POST /reservas — agendar una cita.
      //Valida disponibilidad con MS-Agenda.
     
    @PostMapping
    public ResponseEntity<ReservaResponse> crearReserva(
            @Valid @RequestBody CrearReservaRequest req,
            Authentication auth) {
        UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservaService.crearReserva(req, principal));
    }

    //GET /reservas/mis-citas — mis reservas históricas.
    
    @GetMapping("/mis-citas")
    public ResponseEntity<List<ReservaResponse>> misReservas(Authentication auth) {
        UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(reservaService.misReservas(principal));
    }

    
     //GET /reservas/{id} — ver detalle de una reserva propia.
     
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> obtenerReserva(
            @PathVariable Integer id,
            Authentication auth) {
        UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(reservaService.obtenerReserva(id, principal));
    }

    
     // DELETE /reservas/{id} — cancelar mi reserva.
     
    @DeleteMapping("/{id}")
    public ResponseEntity<ReservaResponse> cancelarReserva(
            @PathVariable Integer id,
            @RequestBody(required = false) CancelarReservaRequest req,
            Authentication auth) {
        UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
        if (req == null) req = new CancelarReservaRequest();
        return ResponseEntity.ok(reservaService.cancelarReserva(id, req, principal));
    }

    // ENDPOINT INTERNO (usado por MS-Agenda)

    
     // GET /reservas/horas-ocupadas?fecha=2025-07-15
    // Lo consume MS-Agenda para calcular qué slots están libres.
     //Requiere JWT válido (comunicación interna entre microservicios).
    
    @GetMapping("/horas-ocupadas")
    public ResponseEntity<List<String>> horasOcupadas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(reservaService.obtenerHorasOcupadas(fecha));
    }

    // ADMIN 

    // GET /reservas/admin/fecha?fecha=2025-07-15 — reservas activas de un día.
    
    @GetMapping("/admin/fecha")
    public ResponseEntity<List<ReservaResponse>> reservasPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(reservaService.listarReservasPorFecha(fecha));
    }

    //GET /reservas/admin/rango?desde=2025-07-01&hasta=2025-07-31 — reservas en rango.

    @GetMapping("/admin/rango")
    public ResponseEntity<List<ReservaResponse>> reservasPorRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(reservaService.listarReservasPorRango(desde, hasta));
    }

     //DELETE /reservas/admin/{id} — cancelar cualquier reserva (admin).
    
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<ReservaResponse> cancelarReservaAdmin(
            @PathVariable Integer id,
            @RequestBody(required = false) CancelarReservaRequest req) {
        if (req == null) req = new CancelarReservaRequest();
        return ResponseEntity.ok(reservaService.cancelarReservaAdmin(id, req));
    }
}

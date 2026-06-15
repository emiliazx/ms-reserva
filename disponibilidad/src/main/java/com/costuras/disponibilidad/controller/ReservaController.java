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

  

     
    @PostMapping
    public ResponseEntity<ReservaResponse> crearReserva(
            @Valid @RequestBody CrearReservaRequest req,
            Authentication auth) {
        UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservaService.crearReserva(req, principal));
    }

   
    
    @GetMapping("/mis-citas")
    public ResponseEntity<List<ReservaResponse>> misReservas(Authentication auth) {
        UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(reservaService.misReservas(principal));
    }

    
    
     
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> obtenerReserva(
            @PathVariable Integer id,
            Authentication auth) {
        UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(reservaService.obtenerReserva(id, principal));
    }

    
    
     
    @DeleteMapping("/{id}")
    public ResponseEntity<ReservaResponse> cancelarReserva(
            @PathVariable Integer id,
            @RequestBody(required = false) CancelarReservaRequest req,
            Authentication auth) {
        UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
        if (req == null) req = new CancelarReservaRequest();
        return ResponseEntity.ok(reservaService.cancelarReserva(id, req, principal));
    }

    
    
    @GetMapping("/horas-ocupadas")
    public ResponseEntity<List<String>> horasOcupadas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(reservaService.obtenerHorasOcupadas(fecha));
    }

    
    
    @GetMapping("/admin/fecha")
    public ResponseEntity<List<ReservaResponse>> reservasPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(reservaService.listarReservasPorFecha(fecha));
    }

   

    @GetMapping("/admin/rango")
    public ResponseEntity<List<ReservaResponse>> reservasPorRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(reservaService.listarReservasPorRango(desde, hasta));
    }

    
    
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<ReservaResponse> cancelarReservaAdmin(
            @PathVariable Integer id,
            @RequestBody(required = false) CancelarReservaRequest req) {
        if (req == null) req = new CancelarReservaRequest();
        return ResponseEntity.ok(reservaService.cancelarReservaAdmin(id, req));
    }
}

package com.costuras.disponibilidad.controller;


import com.costuras.disponibilidad.dto.CancelarReservaRequest;
import com.costuras.disponibilidad.dto.CrearReservaRequest;
import com.costuras.disponibilidad.dto.ReservaResponse;
import com.costuras.disponibilidad.security.UsuarioPrincipal;
import com.costuras.disponibilidad.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "Gestión de reservas y citas de clientes")
public class ReservaController {

    private final ReservaService reservaService;

    @Operation(summary = "Crear reserva",
               description = "Crea una nueva reserva validando disponibilidad en la agenda. Envía confirmación por email.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Reserva creada correctamente"),
        @ApiResponse(responseCode = "400", description = "Slot no disponible o datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping
    public ResponseEntity<ReservaResponse> crearReserva(
            @Valid @RequestBody CrearReservaRequest req, Authentication auth) {
        UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservaService.crearReserva(req, principal));
    }

    @Operation(summary = "Ver mis citas", description = "Obtiene todas las reservas del cliente autenticado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de reservas obtenida"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/mis-citas")
    public ResponseEntity<List<ReservaResponse>> misReservas(Authentication auth) {
        UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(reservaService.misReservas(principal));
    }

    @Operation(summary = "Obtener reserva por ID", description = "Retorna el detalle de una reserva específica.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reserva encontrada"),
        @ApiResponse(responseCode = "403", description = "Sin permiso para ver esta reserva"),
        @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> obtenerReserva(
            @PathVariable Integer id, Authentication auth) {
        UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(reservaService.obtenerReserva(id, principal));
    }

    @Operation(summary = "Cancelar reserva",
               description = "El cliente cancela su reserva. No se puede cancelar reservas pasadas o ya canceladas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reserva cancelada correctamente"),
        @ApiResponse(responseCode = "400", description = "No se puede cancelar (ya cancelada o fecha pasada)"),
        @ApiResponse(responseCode = "403", description = "Sin permiso para cancelar esta reserva")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ReservaResponse> cancelarReserva(
            @PathVariable Integer id,
            @RequestBody(required = false) CancelarReservaRequest req,
            Authentication auth) {
        UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
        if (req == null) req = new CancelarReservaRequest();
        return ResponseEntity.ok(reservaService.cancelarReserva(id, req, principal));
    }

    @Operation(summary = "Horas ocupadas por fecha",
               description = "Retorna los horarios ya reservados para una fecha específica.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de horas ocupadas")
    })
    @GetMapping("/horas-ocupadas")
    public ResponseEntity<List<String>> horasOcupadas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(reservaService.obtenerHorasOcupadas(fecha));
    }

    @Operation(summary = "Reservas por fecha (ADMIN)", description = "Lista todas las reservas activas de una fecha. Requiere rol ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reservas obtenidas"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @GetMapping("/admin/fecha")
    public ResponseEntity<List<ReservaResponse>> reservasPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(reservaService.listarReservasPorFecha(fecha));
    }

    @Operation(summary = "Reservas por rango de fechas (ADMIN)", description = "Lista reservas dentro de un rango. Requiere rol ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reservas obtenidas"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @GetMapping("/admin/rango")
    public ResponseEntity<List<ReservaResponse>> reservasPorRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(reservaService.listarReservasPorRango(desde, hasta));
    }

    @Operation(summary = "Cancelar reserva (ADMIN)", description = "El administrador cancela cualquier reserva.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reserva cancelada por admin"),
        @ApiResponse(responseCode = "400", description = "La reserva ya estaba cancelada"),
        @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<ReservaResponse> cancelarReservaAdmin(
            @PathVariable Integer id,
            @RequestBody(required = false) CancelarReservaRequest req) {
        if (req == null) req = new CancelarReservaRequest();
        return ResponseEntity.ok(reservaService.cancelarReservaAdmin(id, req));
    }
}
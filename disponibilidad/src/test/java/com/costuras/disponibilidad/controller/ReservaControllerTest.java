package com.costuras.disponibilidad.controller;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import com.costuras.disponibilidad.dto.CancelarReservaRequest;
import com.costuras.disponibilidad.dto.CrearReservaRequest;
import com.costuras.disponibilidad.dto.ReservaResponse;
import com.costuras.disponibilidad.model.EstadoReserva;
import com.costuras.disponibilidad.security.UsuarioPrincipal;
import com.costuras.disponibilidad.service.ReservaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@WebMvcTest(ReservaController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservaService reservaService;

    private ObjectMapper objectMapper;
    private UsuarioPrincipal principal;
    private ReservaResponse reservaResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        principal = UsuarioPrincipal.builder()
                .id(1).username("ana").role("USER").build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));

        reservaResponse = new ReservaResponse();
        reservaResponse.setId(1);
        reservaResponse.setEstado(EstadoReserva.CONFIRMADA);
        reservaResponse.setNombreCliente("Ana");
        reservaResponse.setFecha(LocalDate.now().plusDays(3));
        reservaResponse.setHoraInicio(LocalTime.of(10, 0));
    }

 @Test
    void crearReserva_datosValidos_retorna201() throws Exception {
        CrearReservaRequest request = new CrearReservaRequest();
        request.setFecha(LocalDate.now().plusDays(3));
        request.setHoraInicio(LocalTime.of(10, 0));
        request.setEmailCliente("ana@mail.com");
        request.setNombreCliente("Ana");

       
        when(reservaService.crearReserva(any(CrearReservaRequest.class), any(UsuarioPrincipal.class)))
                .thenReturn(reservaResponse);

       
        UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(principal, null, List.of());

        mockMvc.perform(post("/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(auth)) 
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
    }

   @Test
    void crearReserva_slotOcupado_retorna400() throws Exception { 
        CrearReservaRequest request = new CrearReservaRequest();
        request.setFecha(LocalDate.now().plusDays(3));
        request.setHoraInicio(LocalTime.of(10, 0));
        
        request.setEmailCliente("ana@mail.com");
        request.setNombreCliente("Ana");

       
        when(reservaService.crearReserva(any(CrearReservaRequest.class), any(UsuarioPrincipal.class)))
                .thenThrow(new IllegalArgumentException("Slot no disponible"));

        UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(principal, null, List.of());

        mockMvc.perform(post("/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(auth)) 
               
                .andExpect(status().isBadRequest()); 
    }
 
  @Test
    void misReservas_retornaLista() throws Exception {
       
        when(reservaService.misReservas(any(UsuarioPrincipal.class)))
                .thenReturn(List.of(reservaResponse));

        
        UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(principal, null, List.of());

        mockMvc.perform(get("/reservas/mis-citas")
                .principal(auth)) 
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("CONFIRMADA"))
                .andExpect(jsonPath("$[0].nombreCliente").value("Ana"));
    }

   @Test
    void obtenerReserva_propia_retorna200() throws Exception {
       
        when(reservaService.obtenerReserva(eq(1), any(UsuarioPrincipal.class)))
                .thenReturn(reservaResponse);

        
        UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(principal, null, List.of());

        mockMvc.perform(get("/reservas/1")
                .principal(auth)) 
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    
}
 
@Test
    void cancelarReserva_valida_retorna200() throws Exception {
        reservaResponse.setEstado(EstadoReserva.CANCELADA);
        
       
        when(reservaService.cancelarReserva(eq(1), any(CancelarReservaRequest.class), any(UsuarioPrincipal.class)))
                .thenReturn(reservaResponse);

      
        UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(principal, null, List.of());

        mockMvc.perform(delete("/reservas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CancelarReservaRequest()))
                .principal(auth)) 
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }


    @Test
    void horasOcupadas_retornaLista() throws Exception {
        when(reservaService.obtenerHorasOcupadas(any()))
                .thenReturn(List.of("10:00", "11:00"));

        mockMvc.perform(get("/reservas/horas-ocupadas")
                .param("fecha", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("10:00"));
    }



    @Test
    void reservasPorFecha_admin_retornaLista() throws Exception {
        when(reservaService.listarReservasPorFecha(any())).thenReturn(List.of(reservaResponse));

        mockMvc.perform(get("/reservas/admin/fecha")
                .param("fecha", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    } 
}




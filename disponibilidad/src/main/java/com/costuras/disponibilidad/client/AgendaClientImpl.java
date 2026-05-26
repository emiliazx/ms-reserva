package com.costuras.disponibilidad.client;

import com.costuras.disponibilidad.service.AgendaClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;


 //Cliente HTTP hacia MS-Agenda.
 //Consulta si un slot está disponible según la configuración de horarios,
 //festivos y bloqueos manuales.
 
@Slf4j
@Component
public class AgendaClientImpl implements AgendaClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AgendaClientImpl(@Value("${ms.agenda.url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean esSlotValido(LocalDate fecha, LocalTime horaInicio) {
        try {
            String url = baseUrl + "/agenda/disponibilidad?fecha=" + fecha;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || Boolean.FALSE.equals(response.get("disponible"))) return false;

            List<Map<String, Object>> slots = (List<Map<String, Object>>) response.get("slots");
            if (slots == null) return false;

            return slots.stream().anyMatch(slot ->
                horaInicio.toString().equals(slot.get("horaInicio")) &&
                Boolean.TRUE.equals(slot.get("libre"))
            );
        } catch (Exception e) {
            log.warn("No se pudo consultar MS-Agenda para validar slot: {}", e.getMessage());
            // Si MS-Agenda no responde, se permite la reserva (fail-open)
            return true;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public int obtenerDuracionSlot(LocalDate fecha, LocalTime horaInicio) {
        try {
            String url = baseUrl + "/agenda/disponibilidad?fecha=" + fecha;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) return 60; // default 60 min si no se puede consultar

            List<Map<String, Object>> slots = (List<Map<String, Object>>) response.get("slots");
            if (slots == null) return 60;

            return slots.stream()
                .filter(slot -> horaInicio.toString().equals(slot.get("horaInicio")))
                .findFirst()
                .map(slot -> {
                    LocalTime inicio = LocalTime.parse(slot.get("horaInicio").toString());
                    LocalTime fin    = LocalTime.parse(slot.get("horaFin").toString());
                    return (int) java.time.Duration.between(inicio, fin).toMinutes();
                })
                .orElse(60);
        } catch (Exception e) {
            log.warn("No se pudo obtener duración del slot desde MS-Agenda: {}", e.getMessage());
            return 60;
        }
    }
}

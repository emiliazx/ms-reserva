package com.costuras.disponibilidad.client;

import com.costuras.disponibilidad.dto.NotificacionCitaRequest;
import com.costuras.disponibilidad.service.NotificacionesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


 

@Slf4j
@Component
public class NotificacionesClientImpl implements NotificacionesClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public NotificacionesClientImpl(
            @Value("${ms.notificaciones.url}") String baseUrl
    ) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    @Override
    public void notificarCita(NotificacionCitaRequest request) {
        try {
            String url = baseUrl + "/notificaciones/cita";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<NotificacionCitaRequest> entity = new HttpEntity<>(request, headers);
            restTemplate.postForObject(url, entity, Object.class);
            log.info("Notificación de cita enviada a MS-Notificaciones para reserva {}", request.getIdReserva());
        } catch (Exception e) {
            
            log.warn("No se pudo enviar notificación de cita a MS-Notificaciones: {}", e.getMessage());
        }
    }
}

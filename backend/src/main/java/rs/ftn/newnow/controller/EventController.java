package rs.ftn.newnow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ftn.newnow.dto.EventDTO;
import rs.ftn.newnow.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    @GetMapping("/today")
    public ResponseEntity<List<EventDTO>> getTodayEvents() {
        try {
            List<EventDTO> events = eventService.getTodayEvents();
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Error fetching today's events", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

package rs.ftn.newnow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.dto.EventDTO;
import rs.ftn.newnow.model.Event;
import rs.ftn.newnow.repository.EventRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<EventDTO> getTodayEvents() {
        log.info("Fetching today's events");
        LocalDate today = LocalDate.now();
        List<Event> events = eventRepository.findByDate(today);
        
        return events.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private EventDTO convertToDTO(Event event) {
        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setAddress(event.getAddress());
        dto.setType(event.getType());
        dto.setDate(event.getDate());
        dto.setPrice(event.getPrice());
        dto.setRecurrent(event.getRecurrent());
        
        if (event.getLocation() != null) {
            dto.setLocationId(event.getLocation().getId());
            dto.setLocationName(event.getLocation().getName());
        }
        
        if (event.getImage() != null) {
            dto.setImageUrl(event.getImage().getPath());
        }
        
        return dto;
    }
}

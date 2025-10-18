package rs.ftn.newnow.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import rs.ftn.newnow.model.Event;
import rs.ftn.newnow.model.Location;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EventRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventRepository eventRepository;

    private Location location;

    @BeforeEach
    void setUp() {
        location = new Location();
        location.setName("Test Location");
        location.setAddress("Test Address");
        location.setType("Club");
        location.setDescription("Test");
        location.setCreatedAt(LocalDate.now());
        location.setTotalRating(8.0);
        entityManager.persist(location);

        Event event1 = new Event();
        event1.setName("Concert");
        event1.setAddress("Test Address");
        event1.setType("Music");
        event1.setDate(LocalDate.now());
        event1.setPrice(100.0);
        event1.setRecurrent(true);
        event1.setLocation(location);
        entityManager.persist(event1);

        Event event2 = new Event();
        event2.setName("Party");
        event2.setAddress("Test Address");
        event2.setType("Party");
        event2.setDate(LocalDate.now().plusDays(1));
        event2.setPrice(50.0);
        event2.setRecurrent(false);
        event2.setLocation(location);
        entityManager.persist(event2);

        entityManager.flush();
    }

    @Test
    void shouldFindEventsByDate() {
        List<Event> found = eventRepository.findByDate(LocalDate.now());

        assertEquals(1, found.size());
        assertEquals("Concert", found.get(0).getName());
    }

    @Test
    void shouldFindEventsByLocationId() {
        List<Event> found = eventRepository.findByLocationId(location.getId());

        assertEquals(2, found.size());
    }

    @Test
    void shouldFindEventsByType() {
        List<Event> found = eventRepository.findByTypeContainingIgnoreCase("music");

        assertEquals(1, found.size());
        assertEquals("Concert", found.get(0).getName());
    }

    @Test
    void shouldFindEventsByFilters() {
        List<Event> found = eventRepository.findByFilters(
                LocalDate.now(),
                "Music",
                location.getId(),
                null,
                50.0,
                150.0
        );

        assertEquals(1, found.size());
        assertEquals("Concert", found.get(0).getName());
    }
}

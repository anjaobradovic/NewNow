package rs.ftn.newnow.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import rs.ftn.newnow.model.Location;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class LocationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LocationRepository locationRepository;

    @BeforeEach
    void setUp() {
        Location location1 = new Location();
        location1.setName("Club XYZ");
        location1.setAddress("Main Street 1");
        location1.setType("Club");
        location1.setDescription("Test club");
        location1.setCreatedAt(LocalDate.now());
        location1.setTotalRating(8.5);
        entityManager.persist(location1);

        Location location2 = new Location();
        location2.setName("Bar ABC");
        location2.setAddress("Side Street 2");
        location2.setType("Bar");
        location2.setDescription("Test bar");
        location2.setCreatedAt(LocalDate.now());
        location2.setTotalRating(7.2);
        entityManager.persist(location2);

        entityManager.flush();
    }

    @Test
    void shouldFindByNameContaining() {
        List<Location> found = locationRepository
                .findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrTypeContainingIgnoreCase(
                        "xyz", "", "");

        assertEquals(1, found.size());
        assertEquals("Club XYZ", found.get(0).getName());
    }

    @Test
    void shouldFindByType() {
        List<Location> found = locationRepository.findByType("Club");

        assertEquals(1, found.size());
        assertEquals("Club", found.get(0).getType());
    }

    @Test
    void shouldOrderByRatingDescending() {
        List<Location> found = locationRepository.findTopByOrderByTotalRatingDesc();

        assertTrue(found.size() >= 2);
        assertTrue(found.get(0).getTotalRating() >= found.get(1).getTotalRating());
    }
}

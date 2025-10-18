package rs.ftn.newnow.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import rs.ftn.newnow.model.Location;
import rs.ftn.newnow.model.Manages;
import rs.ftn.newnow.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ManagesRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ManagesRepository managesRepository;

    private User user;
    private Location location;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("manager@test.com");
        user.setPassword("password");
        user.setName("Manager");
        user.setCreatedAt(LocalDate.now());
        entityManager.persist(user);

        location = new Location();
        location.setName("Test Location");
        location.setAddress("Address");
        location.setType("Club");
        location.setDescription("Test");
        location.setCreatedAt(LocalDate.now());
        location.setTotalRating(8.0);
        entityManager.persist(location);

        entityManager.flush();
    }

    @Test
    void shouldFindActiveManagement() {
        Manages manages = new Manages();
        manages.setUser(user);
        manages.setLocation(location);
        manages.setStartDate(LocalDate.now().minusDays(10));
        manages.setEndDate(null);
        entityManager.persistAndFlush(manages);

        List<Manages> found = managesRepository.findActiveManagement(
                user.getId(),
                location.getId(),
                LocalDate.now()
        );

        assertEquals(1, found.size());
    }

    @Test
    void shouldNotFindInactiveManagement() {
        Manages manages = new Manages();
        manages.setUser(user);
        manages.setLocation(location);
        manages.setStartDate(LocalDate.now().minusDays(20));
        manages.setEndDate(LocalDate.now().minusDays(5));
        entityManager.persistAndFlush(manages);

        List<Manages> found = managesRepository.findActiveManagement(
                user.getId(),
                location.getId(),
                LocalDate.now()
        );

        assertEquals(0, found.size());
    }

    @Test
    void shouldFindActiveManagersByLocation() {
        Manages manages1 = new Manages();
        manages1.setUser(user);
        manages1.setLocation(location);
        manages1.setStartDate(LocalDate.now().minusDays(10));
        manages1.setEndDate(null);
        entityManager.persist(manages1);

        Manages manages2 = new Manages();
        manages2.setUser(user);
        manages2.setLocation(location);
        manages2.setStartDate(LocalDate.now().minusDays(30));
        manages2.setEndDate(LocalDate.now().minusDays(5));
        entityManager.persist(manages2);

        entityManager.flush();

        List<Manages> found = managesRepository.findActiveManagersByLocation(
                location.getId(),
                LocalDate.now()
        );

        assertEquals(1, found.size());
    }
}

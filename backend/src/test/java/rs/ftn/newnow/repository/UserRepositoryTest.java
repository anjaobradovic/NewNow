package rs.ftn.newnow.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import rs.ftn.newnow.model.User;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUserByEmail() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setName("Test User");
        user.setCreatedAt(LocalDate.now());
        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByEmail("test@test.com");

        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getName());
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        Optional<User> found = userRepository.findByEmail("nonexistent@test.com");

        assertFalse(found.isPresent());
    }

    @Test
    void shouldCheckIfEmailExists() {
        User user = new User();
        user.setEmail("exists@test.com");
        user.setPassword("password");
        user.setName("Test User");
        user.setCreatedAt(LocalDate.now());
        entityManager.persistAndFlush(user);

        assertTrue(userRepository.existsByEmail("exists@test.com"));
        assertFalse(userRepository.existsByEmail("notexists@test.com"));
    }
}

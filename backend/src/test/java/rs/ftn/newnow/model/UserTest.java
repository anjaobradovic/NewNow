package rs.ftn.newnow.model;

import org.junit.jupiter.api.Test;
import rs.ftn.newnow.model.enums.Role;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldSetCreatedAtOnPrePersist() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setName("Test User");
        
        user.onCreate();
        
        assertNotNull(user.getCreatedAt());
        assertEquals(LocalDate.now(), user.getCreatedAt());
    }

    @Test
    void shouldAddDefaultRoleOnPrePersist() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setName("Test User");
        
        user.onCreate();
        
        assertTrue(user.getRoles().contains(Role.ROLE_USER));
    }

    @Test
    void shouldNotOverrideExistingCreatedAt() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setName("Test User");
        LocalDate pastDate = LocalDate.of(2020, 1, 1);
        user.setCreatedAt(pastDate);
        
        user.onCreate();
        
        assertEquals(pastDate, user.getCreatedAt());
    }
}

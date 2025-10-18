package rs.ftn.newnow.model;

import org.junit.jupiter.api.Test;
import rs.ftn.newnow.model.enums.Role;

import static org.junit.jupiter.api.Assertions.*;

class AdministratorTest {

    @Test
    void shouldAddAdminRoleOnPrePersist() {
        Administrator admin = new Administrator();
        admin.setEmail("admin@test.com");
        admin.setPassword("password");
        admin.setName("Admin User");
        
        admin.onCreate();
        
        assertTrue(admin.getRoles().contains(Role.ROLE_ADMIN));
        assertTrue(admin.getRoles().contains(Role.ROLE_USER));
    }
}

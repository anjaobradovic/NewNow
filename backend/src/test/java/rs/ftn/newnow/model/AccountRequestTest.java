package rs.ftn.newnow.model;

import org.junit.jupiter.api.Test;
import rs.ftn.newnow.model.enums.RequestStatus;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AccountRequestTest {

    @Test
    void shouldSetDefaultStatusOnPrePersist() {
        AccountRequest request = new AccountRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");
        request.setName("Test");
        request.setAddress("Address");
        
        request.onCreate();
        
        assertEquals(RequestStatus.PENDING, request.getStatus());
    }

    @Test
    void shouldSetCreatedAtOnPrePersist() {
        AccountRequest request = new AccountRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");
        request.setName("Test");
        request.setAddress("Address");
        
        request.onCreate();
        
        assertNotNull(request.getCreatedAt());
        assertEquals(LocalDate.now(), request.getCreatedAt());
    }

    @Test
    void shouldNotOverrideExistingStatus() {
        AccountRequest request = new AccountRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");
        request.setName("Test");
        request.setAddress("Address");
        request.setStatus(RequestStatus.ACCEPTED);
        
        request.onCreate();
        
        assertEquals(RequestStatus.ACCEPTED, request.getStatus());
    }
}

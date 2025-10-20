package rs.ftn.newnow.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import rs.ftn.newnow.model.AccountRequest;
import rs.ftn.newnow.model.enums.RequestStatus;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountRequestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountRequestRepository accountRequestRepository;

    @Test
    void shouldFindByStatus() {
        AccountRequest request1 = new AccountRequest();
        request1.setEmail("pending@test.com");
        request1.setPassword("password");
        request1.setName("Pending User");
        request1.setAddress("Address");
        request1.setStatus(RequestStatus.PENDING);
        request1.setCreatedAt(LocalDate.now());
        entityManager.persist(request1);

        AccountRequest request2 = new AccountRequest();
        request2.setEmail("accepted@test.com");
        request2.setPassword("password");
        request2.setName("Accepted User");
        request2.setAddress("Address");
        request2.setStatus(RequestStatus.ACCEPTED);
        request2.setCreatedAt(LocalDate.now());
        entityManager.persist(request2);

        entityManager.flush();

        List<AccountRequest> pending = accountRequestRepository.findByStatus(RequestStatus.PENDING);

        assertEquals(1, pending.size());
        assertEquals("pending@test.com", pending.get(0).getEmail());
    }

    @Test
    void shouldCheckIfEmailExists() {
        AccountRequest request = new AccountRequest();
        request.setEmail("exists@test.com");
        request.setPassword("password");
        request.setName("Test User");
        request.setAddress("Address");
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedAt(LocalDate.now());
        entityManager.persistAndFlush(request);

        assertTrue(accountRequestRepository.existsByEmail("exists@test.com"));
        assertFalse(accountRequestRepository.existsByEmail("notexists@test.com"));
    }
}

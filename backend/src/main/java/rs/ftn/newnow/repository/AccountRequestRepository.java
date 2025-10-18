package rs.ftn.newnow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ftn.newnow.model.AccountRequest;
import rs.ftn.newnow.model.enums.RequestStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRequestRepository extends JpaRepository<AccountRequest, Long> {
    
    List<AccountRequest> findByStatus(RequestStatus status);
    
    Optional<AccountRequest> findByEmail(String email);
    
    boolean existsByEmail(String email);
}

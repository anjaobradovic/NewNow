package rs.ftn.newnow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ftn.newnow.model.AccountRequest;
import rs.ftn.newnow.model.enums.RequestStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRequestRepository extends JpaRepository<AccountRequest, Long> {
    
    List<AccountRequest> findByStatus(RequestStatus status);
    
    Page<AccountRequest> findByStatus(RequestStatus status, Pageable pageable);
    
    @Query("SELECT ar FROM AccountRequest ar WHERE " +
           "(:status IS NULL OR ar.status = :status) AND " +
           "(:q IS NULL OR LOWER(ar.email) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<AccountRequest> findByFilters(@Param("status") RequestStatus status, 
                                       @Param("q") String q, 
                                       Pageable pageable);
    
    Optional<AccountRequest> findByEmail(String email);
    
    boolean existsByEmail(String email);
}

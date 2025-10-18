package rs.ftn.newnow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ftn.newnow.model.Manages;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ManagesRepository extends JpaRepository<Manages, Long> {
    
    List<Manages> findByUserId(Long userId);
    
    List<Manages> findByLocationId(Long locationId);
    
    @Query("SELECT m FROM Manages m WHERE m.user.id = :userId " +
           "AND m.location.id = :locationId " +
           "AND (m.endDate IS NULL OR m.endDate > :now)")
    List<Manages> findActiveManagement(
            @Param("userId") Long userId,
            @Param("locationId") Long locationId,
            @Param("now") LocalDate now);
    
    @Query("SELECT m FROM Manages m WHERE m.location.id = :locationId " +
           "AND (m.endDate IS NULL OR m.endDate > :now)")
    List<Manages> findActiveManagersByLocation(
            @Param("locationId") Long locationId,
            @Param("now") LocalDate now);
}

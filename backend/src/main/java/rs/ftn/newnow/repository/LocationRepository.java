package rs.ftn.newnow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rs.ftn.newnow.model.Location;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    
    List<Location> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrTypeContainingIgnoreCase(
            String name, String address, String type);
    
    List<Location> findByType(String type);
    
    @Query("SELECT l FROM Location l ORDER BY l.totalRating DESC")
    List<Location> findTopByOrderByTotalRatingDesc();
}

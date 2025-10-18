package rs.ftn.newnow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ftn.newnow.model.Event;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    List<Event> findByDate(LocalDate date);
    
    List<Event> findByLocationId(Long locationId);
    
    List<Event> findByTypeContainingIgnoreCase(String type);
    
    @Query("SELECT e FROM Event e WHERE e.location.id = :locationId " +
           "AND e.date BETWEEN :startDate AND :endDate")
    List<Event> findByLocationAndDateRange(
            @Param("locationId") Long locationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT e FROM Event e WHERE e.date = :date " +
           "AND (:type IS NULL OR e.type LIKE %:type%) " +
           "AND (:locationId IS NULL OR e.location.id = :locationId) " +
           "AND (:address IS NULL OR e.address LIKE %:address%) " +
           "AND (:minPrice IS NULL OR e.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR e.price <= :maxPrice)")
    List<Event> findByFilters(
            @Param("date") LocalDate date,
            @Param("type") String type,
            @Param("locationId") Long locationId,
            @Param("address") String address,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice);
}

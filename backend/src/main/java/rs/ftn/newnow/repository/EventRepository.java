package rs.ftn.newnow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ftn.newnow.model.Event;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    @Query("SELECT e FROM Event e WHERE e.deleted = false AND e.date = :date")
    List<Event> findByDate(@Param("date") LocalDate date);
    
    @Query("SELECT e FROM Event e WHERE e.deleted = false AND e.date = :date " +
           "AND e.recurrent = false")
    List<Event> findNonRecurrentByDate(@Param("date") LocalDate date);
    
    @Query("SELECT e FROM Event e WHERE e.deleted = false AND e.location.id = :locationId")
    List<Event> findByLocationId(@Param("locationId") Long locationId);
    
    List<Event> findByTypeContainingIgnoreCase(String type);
    
    @Query("SELECT e FROM Event e WHERE e.location.id = :locationId " +
           "AND e.date >= :dateFrom AND e.deleted = false ORDER BY e.date ASC")
    Page<Event> findUpcomingByLocation(
            @Param("locationId") Long locationId,
            @Param("dateFrom") LocalDate dateFrom,
            Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE e.location.id = :locationId " +
           "AND e.date BETWEEN :startDate AND :endDate AND e.deleted = false")
    List<Event> findByLocationAndDateRange(
            @Param("locationId") Long locationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT e FROM Event e WHERE e.deleted = false " +
           "AND (:type IS NULL OR LOWER(e.type) LIKE LOWER(CONCAT('%', :type, '%'))) " +
           "AND (:locationId IS NULL OR e.location.id = :locationId) " +
           "AND (:address IS NULL OR LOWER(e.address) LIKE LOWER(CONCAT('%', :address, '%'))) " +
           "AND (:minPrice IS NULL OR e.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR e.price <= :maxPrice) " +
           "AND (:isFree IS NULL OR (:isFree = true AND e.price = 0) OR (:isFree = false AND e.price > 0)) " +
           "AND (:isRegular IS NULL OR e.recurrent = :isRegular) " +
           "AND (:date IS NULL OR e.date = :date)")
    Page<Event> findByFilters(
            @Param("type") String type,
            @Param("locationId") Long locationId,
            @Param("address") String address,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("isFree") Boolean isFree,
            @Param("isRegular") Boolean isRegular,
            @Param("date") LocalDate date,
            Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE e.id = :id AND e.deleted = false")
    Optional<Event> findByIdAndNotDeleted(@Param("id") Long id);
    
    @Query("SELECT COUNT(e) FROM Event e WHERE e.id = :eventId " +
           "AND e.recurrent = true AND e.deleted = false " +
           "AND e.date <= :untilDate")
    Long countOccurrencesUntilDate(@Param("eventId") Long eventId, @Param("untilDate") LocalDate untilDate);
}

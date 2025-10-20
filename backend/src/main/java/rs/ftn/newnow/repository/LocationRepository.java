package rs.ftn.newnow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ftn.newnow.model.Location;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    
    Optional<Location> findByIdAndDeletedFalse(Long id);
    
    Page<Location> findByDeletedFalse(Pageable pageable);
    
    @Query("SELECT l FROM Location l WHERE l.deleted = false " +
           "AND (LOWER(l.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(l.address) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(l.type) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Location> searchLocations(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT l FROM Location l WHERE l.deleted = false ORDER BY l.totalRating DESC")
    List<Location> findTopByOrderByTotalRatingDesc(Pageable pageable);
    
    @Query("SELECT l FROM Location l LEFT JOIN l.reviews r " +
           "WHERE l.deleted = false AND r.deleted = false " +
           "GROUP BY l.id ORDER BY COUNT(r.id) DESC, AVG(r.rate.overall) DESC")
    List<Location> findPopularLocations(Pageable pageable);
    
    @Query("SELECT l FROM Location l WHERE l.deleted = false " +
           "AND (LOWER(l.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(l.address) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(l.type) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(l.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Location> searchByQuery(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT l FROM Location l WHERE l.deleted = false " +
           "AND (:type IS NULL OR LOWER(l.type) LIKE LOWER(CONCAT('%', :type, '%'))) " +
           "AND (:address IS NULL OR LOWER(l.address) LIKE LOWER(CONCAT('%', :address, '%')))")
    Page<Location> searchByTypeAndAddress(@Param("type") String type, @Param("address") String address, Pageable pageable);
}

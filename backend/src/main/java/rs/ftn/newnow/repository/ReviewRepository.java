package rs.ftn.newnow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ftn.newnow.model.Review;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    Page<Review> findByLocationIdAndHiddenFalseAndDeletedFalse(Long locationId, Pageable pageable);
    
    List<Review> findByLocationIdAndDeletedFalse(Long locationId);
    
    List<Review> findByUserId(Long userId);
    
    Page<Review> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.location.id = :locationId " +
           "AND r.deleted = false ORDER BY r.createdAt DESC")
    List<Review> findRecentByLocation(@Param("locationId") Long locationId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.event.id = :eventId AND r.deleted = false")
    Integer countByEventId(@Param("eventId") Long eventId);
}

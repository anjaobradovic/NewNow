package rs.ftn.newnow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ftn.newnow.model.Review;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    @Query("SELECT r FROM Review r WHERE r.id = :id AND r.deleted = false")
    Optional<Review> findByIdAndNotDeleted(@Param("id") Long id);
    
    @Query("SELECT r FROM Review r WHERE r.location.id = :locationId " +
           "AND r.deleted = false AND r.deletedByManager = false")
    Page<Review> findByLocationIdAndNotDeleted(@Param("locationId") Long locationId, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.location.id = :locationId " +
           "AND r.deleted = false AND r.deletedByManager = false " +
           "ORDER BY r.rate.overall DESC")
    Page<Review> findByLocationIdOrderByRating(@Param("locationId") Long locationId, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.location.id = :locationId " +
           "AND r.deleted = false AND r.deletedByManager = false " +
           "ORDER BY r.createdAt DESC")
    Page<Review> findByLocationIdOrderByDate(@Param("locationId") Long locationId, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.location.id = :locationId " +
           "AND r.deleted = false AND r.deletedByManager = false " +
           "ORDER BY r.rate.overall ASC")
    Page<Review> findByLocationIdOrderByRatingAsc(@Param("locationId") Long locationId, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.location.id = :locationId " +
           "AND r.deleted = false AND r.deletedByManager = false " +
           "ORDER BY r.createdAt ASC")
    Page<Review> findByLocationIdOrderByDateAsc(@Param("locationId") Long locationId, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.location.id = :locationId " +
           "AND r.deleted = false AND r.deletedByManager = false " +
           "ORDER BY r.createdAt DESC")
    List<Review> findTop3ByLocationId(@Param("locationId") Long locationId, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.user.id = :userId AND r.deleted = false")
    List<Review> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT r FROM Review r WHERE r.user.id = :userId AND r.deleted = false")
    Page<Review> findByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.event.id = :eventId " +
           "AND r.user.id = :userId AND r.deleted = false")
    Optional<Review> findByEventIdAndUserId(@Param("eventId") Long eventId, @Param("userId") Long userId);
}


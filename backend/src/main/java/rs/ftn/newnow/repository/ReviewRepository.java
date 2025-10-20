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
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.location.id = :locationId " +
           "AND r.createdAt BETWEEN :startDate AND :endDate " +
           "AND r.deleted = false AND r.deletedByManager = false")
    Long countByLocationAndDateRange(
            @Param("locationId") Long locationId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT AVG(r.rate.overall) FROM Review r WHERE r.location.id = :locationId " +
           "AND r.createdAt BETWEEN :startDate AND :endDate " +
           "AND r.deleted = false AND r.deletedByManager = false")
    Double calculateAverageRatingByLocationAndDateRange(
            @Param("locationId") Long locationId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT COUNT(DISTINCT r.user.id) FROM Review r WHERE r.location.id = :locationId " +
           "AND r.createdAt BETWEEN :startDate AND :endDate " +
           "AND r.deleted = false AND r.deletedByManager = false")
    Long countDistinctUsersByLocationAndDateRange(
            @Param("locationId") Long locationId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.location.id = :locationId " +
           "AND r.deleted = false AND r.deletedByManager = false")
    Long countByLocationAndNotDeleted(@Param("locationId") Long locationId);
    
    @Query("SELECT AVG(r.rate.overall) FROM Review r WHERE r.location.id = :locationId " +
           "AND r.deleted = false AND r.deletedByManager = false")
    Double calculateAverageRatingByLocation(@Param("locationId") Long locationId);
    
    @Query("SELECT r.event.id, r.event.name, AVG(r.rate.overall), COUNT(r) " +
           "FROM Review r WHERE r.location.id = :locationId " +
           "AND r.deleted = false AND r.deletedByManager = false " +
           "GROUP BY r.event.id, r.event.name " +
           "ORDER BY AVG(r.rate.overall) DESC")
    List<Object[]> findEventRatingsDescByLocation(@Param("locationId") Long locationId, Pageable pageable);
    
    @Query("SELECT r.event.id, r.event.name, AVG(r.rate.overall), COUNT(r) " +
           "FROM Review r WHERE r.location.id = :locationId " +
           "AND r.deleted = false AND r.deletedByManager = false " +
           "GROUP BY r.event.id, r.event.name " +
           "ORDER BY AVG(r.rate.overall) ASC")
    List<Object[]> findEventRatingsAscByLocation(@Param("locationId") Long locationId, Pageable pageable);
    
    @Query("SELECT r.location.id FROM Review r " +
           "WHERE r.createdAt BETWEEN :startDate AND :endDate " +
           "AND r.deleted = false AND r.deletedByManager = false " +
           "GROUP BY r.location.id " +
           "ORDER BY COUNT(r) DESC")
    Long findMostPopularLocationInPeriod(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);
}


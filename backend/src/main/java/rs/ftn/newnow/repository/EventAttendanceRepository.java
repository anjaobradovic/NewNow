package rs.ftn.newnow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ftn.newnow.model.EventAttendance;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface EventAttendanceRepository extends JpaRepository<EventAttendance, Long> {

    @Query("SELECT ea FROM EventAttendance ea WHERE ea.user.id = :userId " +
           "AND ea.event.id = :eventId " +
           "AND ea.attendanceDate = :date " +
           "AND ea.attended = true")
    Optional<EventAttendance> findByUserIdAndEventIdAndDate(
        @Param("userId") Long userId, 
        @Param("eventId") Long eventId,
        @Param("date") LocalDate date
    );

    @Query("SELECT COUNT(ea) FROM EventAttendance ea WHERE ea.user.id = :userId " +
           "AND ea.event.id = :eventId " +
           "AND ea.attendanceDate <= :date " +
           "AND ea.attended = true")
    Long countUserAttendanceForEvent(
        @Param("userId") Long userId, 
        @Param("eventId") Long eventId,
        @Param("date") LocalDate date
    );
}

package rs.ftn.newnow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "manages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Manages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @PrePersist
    protected void onCreate() {
        if (startDate == null) {
            startDate = LocalDate.now();
        }
    }

    public boolean isActive() {
        return endDate == null || endDate.isAfter(LocalDate.now());
    }
}

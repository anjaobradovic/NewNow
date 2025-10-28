package rs.ftn.newnow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private Integer performance;

    @Column(name = "sound_and_lighting", nullable = true)
    private Integer soundAndLighting;

    @Column(nullable = true)
    private Integer venue;

    @Column(name = "overall_impression", nullable = true)
    private Integer overallImpression;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    public Double getAverageRating() {
        int count = 0;
        int sum = 0;
        
        if (performance != null) {
            sum += performance;
            count++;
        }
        if (soundAndLighting != null) {
            sum += soundAndLighting;
            count++;
        }
        if (venue != null) {
            sum += venue;
            count++;
        }
        if (overallImpression != null) {
            sum += overallImpression;
            count++;
        }
        
        return count > 0 ? (double) sum / count : 0.0;
    }
}

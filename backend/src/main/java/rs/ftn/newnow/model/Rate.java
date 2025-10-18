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

    private Integer performance;

    private Integer soundAndLighting;

    private Integer venue;

    private Integer overallImpression;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    public Double getAverageRating() {
        int count = 0;
        double sum = 0.0;

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

        return count > 0 ? sum / count : 0.0;
    }
}

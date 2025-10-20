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

    @Column(nullable = false)
    private Integer performance;

    @Column(name = "sound_light", nullable = false)
    private Integer soundLight;

    @Column(nullable = false)
    private Integer space;

    @Column(nullable = false)
    private Integer overall;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    public Double getAverageRating() {
        return (performance + soundLight + space + overall) / 4.0;
    }
}

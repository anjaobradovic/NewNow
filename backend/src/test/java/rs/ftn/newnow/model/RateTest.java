package rs.ftn.newnow.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateTest {

    @Test
    void shouldCalculateAverageRatingWithAllFields() {
        Rate rate = new Rate();
        rate.setPerformance(8);
        rate.setSoundLight(9);
        rate.setSpace(7);
        rate.setOverall(10);
        
        Double average = rate.getAverageRating();
        
        assertEquals(8.5, average, 0.01);
    }
}

package rs.ftn.newnow.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateTest {

    @Test
    void shouldCalculateAverageRatingWithAllFields() {
        Rate rate = new Rate();
        rate.setPerformance(8);
        rate.setSoundAndLighting(9);
        rate.setVenue(7);
        rate.setOverallImpression(10);
        
        Double average = rate.getAverageRating();
        
        assertEquals(8.5, average, 0.01);
    }
}

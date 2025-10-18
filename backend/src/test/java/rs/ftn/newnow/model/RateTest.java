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

    @Test
    void shouldCalculateAverageRatingWithSomeNullFields() {
        Rate rate = new Rate();
        rate.setPerformance(8);
        rate.setSoundAndLighting(null);
        rate.setVenue(6);
        rate.setOverallImpression(null);
        
        Double average = rate.getAverageRating();
        
        assertEquals(7.0, average, 0.01);
    }

    @Test
    void shouldReturnZeroWhenAllFieldsAreNull() {
        Rate rate = new Rate();
        
        Double average = rate.getAverageRating();
        
        assertEquals(0.0, average);
    }
}

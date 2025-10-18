package rs.ftn.newnow.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ManagesTest {

    @Test
    void shouldBeActiveWhenEndDateIsNull() {
        Manages manages = new Manages();
        manages.setStartDate(LocalDate.now().minusDays(10));
        manages.setEndDate(null);
        
        assertTrue(manages.isActive());
    }

    @Test
    void shouldBeActiveWhenEndDateIsInFuture() {
        Manages manages = new Manages();
        manages.setStartDate(LocalDate.now().minusDays(10));
        manages.setEndDate(LocalDate.now().plusDays(10));
        
        assertTrue(manages.isActive());
    }

    @Test
    void shouldNotBeActiveWhenEndDateIsInPast() {
        Manages manages = new Manages();
        manages.setStartDate(LocalDate.now().minusDays(20));
        manages.setEndDate(LocalDate.now().minusDays(5));
        
        assertFalse(manages.isActive());
    }

    @Test
    void shouldSetStartDateOnPrePersist() {
        Manages manages = new Manages();
        
        manages.onCreate();
        
        assertNotNull(manages.getStartDate());
        assertEquals(LocalDate.now(), manages.getStartDate());
    }
}

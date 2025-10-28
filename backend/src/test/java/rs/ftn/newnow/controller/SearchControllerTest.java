package rs.ftn.newnow.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.model.Event;
import rs.ftn.newnow.model.Location;
import rs.ftn.newnow.repository.EventRepository;
import rs.ftn.newnow.repository.LocationRepository;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private EventRepository eventRepository;

    private Location testLocation;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        testLocation = new Location();
        testLocation.setName("Test Location");
        testLocation.setAddress("Test Address 123");
        testLocation.setType("bar");
        testLocation.setDescription("Test Description");
        testLocation.setDeleted(false);
        testLocation = locationRepository.save(testLocation);

        testEvent = new Event();
        testEvent.setName("Test Event");
        testEvent.setAddress("Event Address");
        testEvent.setType("concert");
        testEvent.setDate(LocalDate.now().plusDays(7));
        testEvent.setPrice(50.0);
        testEvent.setRecurrent(false);
        testEvent.setDeleted(false);
        testEvent.setLocation(testLocation);
        testEvent = eventRepository.save(testEvent);
    }

    @Test
    void searchLocations_WithQueryParam_ShouldReturnMatchingLocations() throws Exception {
        mockMvc.perform(get("/api/search/locations")
                        .param("q", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].name", containsString("Test")));
    }

    @Test
    void searchLocations_WithTypeParam_ShouldReturnMatchingLocations() throws Exception {
        mockMvc.perform(get("/api/search/locations")
                        .param("type", "bar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].type", is("bar")));
    }

    @Test
    void searchLocations_WithAddressParam_ShouldReturnMatchingLocations() throws Exception {
        mockMvc.perform(get("/api/search/locations")
                        .param("address", "Test Address"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].address", containsString("Test Address")));
    }

    @Test
    void searchLocations_WithPagination_ShouldReturnPaginatedResults() throws Exception {
        mockMvc.perform(get("/api/search/locations")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size", is(5)));
    }

    @Test
    void searchEvents_WithTypeParam_ShouldReturnMatchingEvents() throws Exception {
        mockMvc.perform(get("/api/search/events")
                        .param("type", "concert"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].type", anyOf(is("concert"), is("Concert"))));
    }

    @Test
    void searchEvents_WithLocationIdParam_ShouldReturnMatchingEvents() throws Exception {
        mockMvc.perform(get("/api/search/events")
                        .param("locationId", testLocation.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].locationId", is(testLocation.getId().intValue())));
    }

    @Test
    void searchEvents_WithPriceRange_ShouldReturnMatchingEvents() throws Exception {
        mockMvc.perform(get("/api/search/events")
                        .param("minPrice", "40")
                        .param("maxPrice", "60"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].price", allOf(greaterThanOrEqualTo(40.0), lessThanOrEqualTo(60.0))));
    }

    @Test
    void searchEvents_WithFutureFlag_ShouldReturnFutureEvents() throws Exception {
        mockMvc.perform(get("/api/search/events")
                        .param("future", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void searchEvents_WithDateRange_ShouldReturnEventsInRange() throws Exception {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusMonths(1);

        mockMvc.perform(get("/api/search/events")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void searchEvents_WithPagination_ShouldReturnPaginatedResults() throws Exception {
        mockMvc.perform(get("/api/search/events")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size", is(5)));
    }
}

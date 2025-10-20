package rs.ftn.newnow.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.model.Event;
import rs.ftn.newnow.model.Location;
import rs.ftn.newnow.repository.EventRepository;
import rs.ftn.newnow.repository.LocationRepository;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;

    public DataInitializer(LocationRepository locationRepository, EventRepository eventRepository) {
        this.locationRepository = locationRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seed();
    }

    @Transactional
    public void run() { // overload for manual trigger
        seed();
    }

    private void seed() {
        // Seed only once to avoid duplicates
        if (locationRepository.count() > 0 || eventRepository.count() > 0) {
            return;
        }

        // Novi Sad
        Location noviSad = new Location();
        noviSad.setName("Novi Sad Arena");
        noviSad.setDescription("Modern multi-purpose arena in Novi Sad hosting concerts and festivals.");
        noviSad.setAddress("Bulevar Oslobođenja 100, Novi Sad");
        noviSad.setType("Arena");
        locationRepository.save(noviSad);

        // Bijelo Polje
        Location bijeloPolje = new Location();
        bijeloPolje.setName("Centar Bijelo Polje");
        bijeloPolje.setDescription("Cultural center in the heart of Bijelo Polje.");
        bijeloPolje.setAddress("Trg Slobode 1, Bijelo Polje");
        bijeloPolje.setType("Cultural Center");
        locationRepository.save(bijeloPolje);

        // Budapest
        Location budapest = new Location();
        budapest.setName("Budapest Park");
        budapest.setDescription("Open-air venue for music and street-food events in Budapest.");
        budapest.setAddress("Soroksári út 60, Budapest");
        budapest.setType("Park");
        locationRepository.save(budapest);

        // Events for Novi Sad
        createEvent(noviSad, "Rock Night", "Bulevar Oslobođenja 100, Novi Sad", "Concert", LocalDate.now().plusDays(1), 1200.0, false);
        createEvent(noviSad, "Wine Festival", "Petrovaradin Fortress, Novi Sad", "Festival", LocalDate.now().plusWeeks(3), 0.0, false);

        // Events for Bijelo Polje
        createEvent(bijeloPolje, "Folklore Evening", "Trg Slobode 1, Bijelo Polje", "Culture", LocalDate.now().minusDays(5), 0.0, false);
        createEvent(bijeloPolje, "Art Exhibition", "Muzej Bijelo Polje", "Exhibition", LocalDate.now().plusDays(10), 0.0, false);

        // Events for Budapest
        createEvent(budapest, "Jazz at the Park", "Soroksári út 60, Budapest", "Concert", LocalDate.now().plusDays(2), 2000.0, false);
        createEvent(budapest, "Food Truck Fiesta", "Rákóczi Bridge, Budapest", "Food", LocalDate.now().plusWeeks(2), 0.0, true);
    }

    private void createEvent(Location location, String name, String address, String type, LocalDate date, Double price, boolean recurrent) {
        Event e = new Event();
        e.setName(name);
        e.setAddress(address);
        e.setType(type);
        e.setDate(date);
        e.setPrice(price != null ? price : 0.0);
        e.setRecurrent(recurrent);
        e.setLocation(location);
        eventRepository.save(e);
    }
}

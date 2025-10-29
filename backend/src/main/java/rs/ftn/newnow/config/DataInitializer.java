package rs.ftn.newnow.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.model.*;
import rs.ftn.newnow.repository.*;

import java.time.LocalDate;

@Component
@Order(2)
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final ManagesRepository managesRepository;

    public DataInitializer(LocationRepository locationRepository,
                           EventRepository eventRepository,
                           ImageRepository imageRepository,
                           UserRepository userRepository,
                           ManagesRepository managesRepository) {
        this.locationRepository = locationRepository;
        this.eventRepository = eventRepository;
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
        this.managesRepository = managesRepository;
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
        noviSad.setImageUrl("/uploads/locations/novi-sad-arena.jpg");
        locationRepository.save(noviSad);

        // Bijelo Polje
        Location bijeloPolje = new Location();
        bijeloPolje.setName("Centar Bijelo Polje");
        bijeloPolje.setDescription("Cultural center in the heart of Bijelo Polje.");
        bijeloPolje.setAddress("Trg Slobode 1, Bijelo Polje");
        bijeloPolje.setType("Cultural Center");
        bijeloPolje.setImageUrl("/uploads/locations/bijelo-polje-center.jpg");
        locationRepository.save(bijeloPolje);

        // Budapest
        Location budapest = new Location();
        budapest.setName("Budapest Park");
        budapest.setDescription("Open-air venue for music and street-food events in Budapest.");
        budapest.setAddress("Soroksári út 60, Budapest");
        budapest.setType("Park");
        budapest.setImageUrl("/uploads/locations/budapest-park.jpg");
        locationRepository.save(budapest);

        // Events for Novi Sad
        // Events happening today (October 29, 2025)
        Event todayEvent1 = createEvent(noviSad, "Live Band Performance", "Novi Sad Arena", "Concert", LocalDate.of(2025, 10, 29), 1500.0, false);
        attachEventImage(todayEvent1, "/uploads/events/live-band-performance.jpg");
        Event todayEvent2 = createEvent(noviSad, "Tech Conference 2025", "SPENS, Novi Sad", "Conference", LocalDate.of(2025, 10, 29), 0.0, false);
        attachEventImage(todayEvent2, "/uploads/events/tech-conference.jpg");
        
        // Upcoming events
        Event e1 = createEvent(noviSad, "Rock Night", "Bulevar Oslobođenja 100, Novi Sad", "Concert", LocalDate.now().plusDays(1), 1200.0, false);
        attachEventImage(e1, "/uploads/events/rock-night.jpg");
        Event e2 = createEvent(noviSad, "Wine Festival", "Petrovaradin Fortress, Novi Sad", "Festival", LocalDate.now().plusWeeks(3), 0.0, false);
        attachEventImage(e2, "/uploads/events/wine-festival.jpg");
        Event e3 = createEvent(noviSad, "Startup Meetup", "SPENS, Novi Sad", "Meetup", LocalDate.now().plusDays(7), 0.0, false);
        attachEventImage(e3, "/uploads/events/startup-meetup.jpg");
        // Past events for Novi Sad (reviewable)
        Event e10 = createEvent(noviSad, "Electronic Music Night", "Bulevar Oslobođenja 100, Novi Sad", "Concert", LocalDate.now().minusDays(10), 1500.0, true);
        attachEventImage(e10, "/uploads/events/electronic-night.jpg");
        Event e11 = createEvent(noviSad, "Jazz Evening", "SPENS, Novi Sad", "Concert", LocalDate.now().minusDays(25), 800.0, true);
        attachEventImage(e11, "/uploads/events/jazz-evening.jpg");
        Event e12 = createEvent(noviSad, "Comedy Show", "Novi Sad Arena", "Entertainment", LocalDate.now().minusDays(5), 1000.0, false);
        attachEventImage(e12, "/uploads/events/comedy-show.jpg");

        // Events for Bijelo Polje
        Event e4 = createEvent(bijeloPolje, "Folklore Evening", "Trg Slobode 1, Bijelo Polje", "Culture", LocalDate.now().minusDays(5), 0.0, false);
        attachEventImage(e4, "/uploads/events/folklore-evening.jpg");
        Event e5 = createEvent(bijeloPolje, "Art Exhibition", "Muzej Bijelo Polje", "Exhibition", LocalDate.now().plusDays(10), 0.0, false);
        attachEventImage(e5, "/uploads/events/art-exhibition.jpg");
        Event e6 = createEvent(bijeloPolje, "Local Food Fair", "Gradski Trg", "Food", LocalDate.now().plusWeeks(1), 0.0, true);
        attachEventImage(e6, "/uploads/events/food-fair.jpg");
        // Past events for Bijelo Polje (reviewable)
        Event e13 = createEvent(bijeloPolje, "Traditional Music Night", "Trg Slobode 1, Bijelo Polje", "Culture", LocalDate.now().minusDays(15), 0.0, true);
        attachEventImage(e13, "/uploads/events/traditional-music.jpg");
        Event e14 = createEvent(bijeloPolje, "Book Fair", "Centar Bijelo Polje", "Culture", LocalDate.now().minusDays(30), 0.0, false);
        attachEventImage(e14, "/uploads/events/book-fair.jpg");

        // Events for Budapest
        Event e7 = createEvent(budapest, "Jazz at the Park", "Soroksári út 60, Budapest", "Concert", LocalDate.now().plusDays(2), 2000.0, false);
        attachEventImage(e7, "/uploads/events/jazz-at-the-park.jpg");
        Event e8 = createEvent(budapest, "Food Truck Fiesta", "Rákóczi Bridge, Budapest", "Food", LocalDate.now().plusWeeks(2), 0.0, true);
        attachEventImage(e8, "/uploads/events/food-truck-fiesta.jpg");
        Event e9 = createEvent(budapest, "Open-Air Cinema", "Budapest Park", "Cinema", LocalDate.now().plusDays(12), 800.0, false);
        attachEventImage(e9, "/uploads/events/open-air-cinema.jpg");
        // Past events for Budapest (reviewable)
        Event e15 = createEvent(budapest, "Summer Music Festival", "Soroksári út 60, Budapest", "Festival", LocalDate.now().minusDays(20), 2500.0, true);
        attachEventImage(e15, "/uploads/events/summer-festival.jpg");
        Event e16 = createEvent(budapest, "Street Food Weekend", "Budapest Park", "Food", LocalDate.now().minusDays(12), 0.0, true);
        attachEventImage(e16, "/uploads/events/street-food.jpg");
        Event e17 = createEvent(budapest, "Indie Band Night", "Budapest Park", "Concert", LocalDate.now().minusDays(8), 1800.0, false);
        attachEventImage(e17, "/uploads/events/indie-band.jpg");

        // Attach gallery images to locations (optional extra pictures)
        attachLocationImage(noviSad, "/uploads/locations/novi-sad-arena-2.jpg");
        attachLocationImage(bijeloPolje, "/uploads/locations/bijelo-polje-center-2.jpg");
        attachLocationImage(budapest, "/uploads/locations/budapest-park-2.jpg");

        // Assign managers to locations (managers created in DataLoader @Order(1))
        assignManagerToLocation("manager.novisad@newnow.com", noviSad);
        assignManagerToLocation("manager.bijelopolje@newnow.com", bijeloPolje);
        assignManagerToLocation("manager.budapest@newnow.com", budapest);

        // Reviews and ratings will be added by users after attending events
    }

    private Event createEvent(Location location, String name, String address, String type, LocalDate date, Double price, boolean recurrent) {
        Event e = new Event();
        e.setName(name);
        e.setAddress(address);
        e.setType(type);
        e.setDate(date);
        e.setPrice(price != null ? price : 0.0);
        e.setRecurrent(recurrent);
        e.setLocation(location);
        return eventRepository.save(e);
    }

    private void attachEventImage(Event event, String path) {
        Image img = new Image();
        img.setPath(path);
        img.setEvent(event);
        imageRepository.save(img);
    }

    private void attachLocationImage(Location location, String path) {
        Image img = new Image();
        img.setPath(path);
        img.setLocation(location);
        imageRepository.save(img);
    }

    private void assignManagerToLocation(String email, Location location) {
        userRepository.findByEmail(email).ifPresent(user -> {
            Manages manages = new Manages();
            manages.setUser(user);
            manages.setLocation(location);
            managesRepository.save(manages);
        });
    }
}

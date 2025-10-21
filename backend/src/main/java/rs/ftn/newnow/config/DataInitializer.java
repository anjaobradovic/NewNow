package rs.ftn.newnow.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.model.*;
import rs.ftn.newnow.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
@Order(2)
public class DataInitializer implements CommandLineRunner {

    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final ManagesRepository managesRepository;
    private final ReviewRepository reviewRepository;
    private final RateRepository rateRepository;
    private final CommentRepository commentRepository;

    public DataInitializer(LocationRepository locationRepository,
                           EventRepository eventRepository,
                           ImageRepository imageRepository,
                           UserRepository userRepository,
                           ManagesRepository managesRepository,
                           ReviewRepository reviewRepository,
                           RateRepository rateRepository,
                           CommentRepository commentRepository) {
        this.locationRepository = locationRepository;
        this.eventRepository = eventRepository;
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
        this.managesRepository = managesRepository;
        this.reviewRepository = reviewRepository;
        this.rateRepository = rateRepository;
        this.commentRepository = commentRepository;
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

        // Seed reviews and comments
        seedReviews();
    }

    private void seedReviews() {
        Random random = new Random();
        List<User> users = userRepository.findAll();
        List<Location> locations = locationRepository.findAll();
        List<Event> allEvents = eventRepository.findAll();

        if (users.isEmpty() || locations.isEmpty() || allEvents.isEmpty()) {
            return;
        }

        // Filter only recurrent or past events for reviews
        List<Event> reviewableEvents = allEvents.stream()
                .filter(e -> e.getRecurrent() || e.getDate().isBefore(LocalDate.now()))
                .toList();

        if (reviewableEvents.isEmpty()) {
            return;
        }

        // Review comments pool
        String[] comments = {
                "Amazing experience! The atmosphere was electric and the performance exceeded all expectations.",
                "Great venue with excellent acoustics. The sound quality was top-notch throughout the entire event.",
                "Loved every moment! The organization was flawless and the staff was incredibly friendly.",
                "Fantastic event! The lighting design created a perfect ambiance for the evening.",
                "One of the best events I've attended this year. Can't wait for the next one!",
                "The venue was spacious and comfortable. Perfect for this type of event.",
                "Really enjoyed the experience. The variety of performances kept it interesting all night.",
                "Excellent sound system and great crowd. Would definitely come back again.",
                "The location is perfect and easy to access. Parking was also convenient.",
                "Wonderful atmosphere and great energy from the crowd. Memorable night!",
                "Professional organization and attention to detail. Everything ran smoothly.",
                "The venue exceeded my expectations. Clean facilities and good visibility from all angles.",
                "Great event with a diverse lineup. Something for everyone to enjoy.",
                "Impressive production quality. The technical aspects were handled perfectly.",
                "Lovely venue with a welcoming vibe. Staff was helpful and accommodating.",
                "Solid experience overall. Good value for the price and well worth attending.",
                "The acoustic quality was outstanding. Every note was crystal clear.",
                "Enjoyable evening with friends. The venue layout worked really well.",
                "Good organization and timing. Events started and ended as scheduled.",
                "Pleasant surprise! Better than I expected in every way."
        };

        // Reply comments pool
        String[] replies = {
                "Thank you for your feedback! We're thrilled you enjoyed the experience.",
                "We appreciate your kind words! Looking forward to seeing you at future events.",
                "Thanks for visiting! Your support means the world to us.",
                "Glad you had a great time! We work hard to make every event special.",
                "Thank you! We're constantly improving and your feedback helps us.",
                "We're so happy you enjoyed it! More exciting events coming soon.",
                "Thanks for the positive review! Hope to see you again.",
                "Your satisfaction is our priority. Thank you for sharing!",
                "Absolutely agree! Glad you noticed the details we put into the event.",
                "Thank you for coming! We hope to create many more memorable experiences.",
                "I had the same experience! The venue really knows how to deliver.",
                "Couldn't agree more! This place never disappoints.",
                "Same here! Already planning my next visit.",
                "Exactly my thoughts! The attention to detail is impressive.",
                "Well said! This venue sets the standard for quality events."
        };

        int reviewCount = 0;
        int targetReviews = 20;

        // Create reviews for each location
        for (Location location : locations) {
            List<Event> locationEvents = reviewableEvents.stream()
                    .filter(e -> e.getLocation().getId().equals(location.getId()))
                    .toList();

            if (locationEvents.isEmpty()) continue;

            // Create 5-8 reviews per location
            int reviewsForLocation = 5 + random.nextInt(4);
            
            for (int i = 0; i < reviewsForLocation && reviewCount < targetReviews; i++) {
                User reviewer = users.get(random.nextInt(users.size()));
                Event event = locationEvents.get(random.nextInt(locationEvents.size()));

                // Create review
                Review review = new Review();
                review.setUser(reviewer);
                review.setLocation(location);
                review.setEvent(event);
                review.setComment(comments[random.nextInt(comments.length)]);
                review.setEventCount(1 + random.nextInt(5)); // 1-5 times attended
                review.setHidden(false);
                review.setDeleted(false);
                review.setDeletedByManager(false);
                review.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(60))); // Within last 60 days
                reviewRepository.save(review);

                // Create ratings
                Rate rate = new Rate();
                rate.setReview(review);
                rate.setPerformance(7 + random.nextInt(4)); // 7-10
                rate.setSoundLight(7 + random.nextInt(4));
                rate.setSpace(6 + random.nextInt(5)); // 6-10
                rate.setOverall(7 + random.nextInt(4));
                rateRepository.save(rate);

                review.setRate(rate);
                reviewRepository.save(review);

                // Add 0-3 comments to this review
                int numComments = random.nextInt(4);
                for (int j = 0; j < numComments; j++) {
                    User commenter = users.get(random.nextInt(users.size()));
                    Comment comment = new Comment();
                    comment.setUser(commenter);
                    comment.setReview(review);
                    comment.setText(replies[random.nextInt(replies.length)]);
                    comment.setDeleted(false);
                    comment.setCreatedAt(review.getCreatedAt().plusHours(random.nextInt(48)));
                    commentRepository.save(comment);

                    // 30% chance of a reply to this comment
                    if (random.nextDouble() < 0.3) {
                        User replier = users.get(random.nextInt(users.size()));
                        Comment reply = new Comment();
                        reply.setUser(replier);
                        reply.setReview(review);
                        reply.setParentComment(comment);
                        reply.setText(replies[random.nextInt(replies.length)]);
                        reply.setDeleted(false);
                        reply.setCreatedAt(comment.getCreatedAt().plusHours(random.nextInt(24)));
                        commentRepository.save(reply);
                    }
                }

                reviewCount++;
            }
        }
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

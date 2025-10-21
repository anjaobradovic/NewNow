package rs.ftn.newnow.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rs.ftn.newnow.model.*;
import rs.ftn.newnow.model.enums.Role;
import rs.ftn.newnow.repository.*;

import java.time.LocalDate;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final AdministratorRepository administratorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // Add repository to store avatar images
    private final ImageRepository imageRepository;

    @Override
    public void run(String... args) {
        long adminCount = administratorRepository.count();
        log.info("=== DataLoader: Current administrator count: {}", adminCount);
        
        if (adminCount == 0) {
            log.info("=== DataLoader: Initializing database with default data...");
            createAdministrators();
            createManagerUsers();
            createRegularUsers();
            log.info("=== DataLoader: Database initialization completed!");
        } else {
            log.info("=== DataLoader: Skipping initialization - administrators already exist (count: {})", adminCount);
        }
    }

    private void createAdministrators() {
        // Admin 1: Anja Obradovic
        Administrator anja = new Administrator();
        anja.setEmail("anja.obradovic@newnow.com");
        anja.setPassword(passwordEncoder.encode("admin123"));
        anja.setName("Anja Obradovic");
        anja.setCreatedAt(LocalDate.now());
        anja.setAddress("Nikole Pašića 12");
        anja.setCity("Novi Sad");
        anja.setPhoneNumber("+381642345678");
        anja.getRoles().add(Role.ROLE_ADMIN);
        anja.getRoles().add(Role.ROLE_USER);
        administratorRepository.save(anja);
        // Attach avatar
        attachAvatar(anja, "/uploads/avatars/anja.jpg");
        log.info("Administrator created: {} ({})", anja.getName(), anja.getEmail());

        // Admin 2: Marko Gordic
        Administrator marko = new Administrator();
        marko.setEmail("marko.gordic@newnow.com");
        marko.setPassword(passwordEncoder.encode("admin123"));
        marko.setName("Marko Gordic");
        marko.setCreatedAt(LocalDate.now());
        marko.setAddress("Bulevar oslobođenja 46");
        marko.setCity("Novi Sad");
        marko.setPhoneNumber("+381641234567");
        marko.getRoles().add(Role.ROLE_ADMIN);
        marko.getRoles().add(Role.ROLE_USER);
        administratorRepository.save(marko);
        // Attach avatar
        attachAvatar(marko, "/uploads/avatars/marko.jpg");
        log.info("Administrator created: {} ({})", marko.getName(), marko.getEmail());
    }

    private void createManagerUsers() {
        // Manager for Novi Sad
        User nsManager = new User();
        nsManager.setEmail("manager.novisad@newnow.com");
        nsManager.setPassword(passwordEncoder.encode("manager123"));
        nsManager.setName("Nikola Manager");
        nsManager.setCreatedAt(LocalDate.now());
        nsManager.setAddress("Bulevar cara Lazara 10");
        nsManager.setCity("Novi Sad");
        nsManager.setPhoneNumber("+38160111222");
        nsManager.getRoles().add(Role.ROLE_USER);
        nsManager.getRoles().add(Role.ROLE_MANAGER);
        userRepository.save(nsManager);
        attachAvatar(nsManager, "/uploads/avatars/manager-novisad.jpg");
        log.info("Manager user created: {} ({})", nsManager.getName(), nsManager.getEmail());

        // Manager for Bijelo Polje
        User bpManager = new User();
        bpManager.setEmail("manager.bijelopolje@newnow.com");
        bpManager.setPassword(passwordEncoder.encode("manager123"));
        bpManager.setName("Milica Manager");
        bpManager.setCreatedAt(LocalDate.now());
        bpManager.setAddress("Trg Slobode 2");
        bpManager.setCity("Bijelo Polje");
        bpManager.setPhoneNumber("+38267111222");
        bpManager.getRoles().add(Role.ROLE_USER);
        bpManager.getRoles().add(Role.ROLE_MANAGER);
        userRepository.save(bpManager);
        attachAvatar(bpManager, "/uploads/avatars/manager-bijelopolje.jpg");
        log.info("Manager user created: {} ({})", bpManager.getName(), bpManager.getEmail());

        // Manager for Budapest
        User bManager = new User();
        bManager.setEmail("manager.budapest@newnow.com");
        bManager.setPassword(passwordEncoder.encode("manager123"));
        bManager.setName("Bence Manager");
        bManager.setCreatedAt(LocalDate.now());
        bManager.setAddress("Soroksári út 12");
        bManager.setCity("Budapest");
        bManager.setPhoneNumber("+3612345678");
        bManager.getRoles().add(Role.ROLE_USER);
        bManager.getRoles().add(Role.ROLE_MANAGER);
        userRepository.save(bManager);
        attachAvatar(bManager, "/uploads/avatars/manager-budapest.jpg");
        log.info("Manager user created: {} ({})", bManager.getName(), bManager.getEmail());
    }

    private void createRegularUsers() {
        // User 1: Petar Petrovic
        User petar = new User();
        petar.setEmail("petar.petrovic@gmail.com");
        petar.setPassword(passwordEncoder.encode("user123"));
        petar.setName("Petar Petrovic");
        petar.setCreatedAt(LocalDate.now());
        petar.setAddress("Kralja Petra 23");
        petar.setCity("Novi Sad");
        petar.setPhoneNumber("+381643456789");
        petar.getRoles().add(Role.ROLE_USER);
        userRepository.save(petar);
        attachAvatar(petar, "/uploads/avatars/petar.jpg");
        log.info("Regular user created: {} ({})", petar.getName(), petar.getEmail());

        // User 2: Jelena Jovic
        User jelena = new User();
        jelena.setEmail("jelena.jovic@gmail.com");
        jelena.setPassword(passwordEncoder.encode("user123"));
        jelena.setName("Jelena Jovic");
        jelena.setCreatedAt(LocalDate.now());
        jelena.setAddress("Svetozara Markovića 15");
        jelena.setCity("Novi Sad");
        jelena.setPhoneNumber("+381644567890");
        jelena.getRoles().add(Role.ROLE_USER);
        userRepository.save(jelena);
        attachAvatar(jelena, "/uploads/avatars/jelena.jpg");
        log.info("Regular user created: {} ({})", jelena.getName(), jelena.getEmail());

        // User 3: Milan Nikolic
        User milan = new User();
        milan.setEmail("milan.nikolic@gmail.com");
        milan.setPassword(passwordEncoder.encode("user123"));
        milan.setName("Milan Nikolic");
        milan.setCreatedAt(LocalDate.now());
        milan.setAddress("Jovana Cvijića 8");
        milan.setCity("Novi Sad");
        milan.setPhoneNumber("+381645678901");
        milan.getRoles().add(Role.ROLE_USER);
        userRepository.save(milan);
        attachAvatar(milan, "/uploads/avatars/milan.jpg");
        log.info("Regular user created: {} ({})", milan.getName(), milan.getEmail());

        // User 4: Ana Kovacevic
        User ana = new User();
        ana.setEmail("ana.kovacevic@gmail.com");
        ana.setPassword(passwordEncoder.encode("user123"));
        ana.setName("Ana Kovacevic");
        ana.setCreatedAt(LocalDate.now());
        ana.setAddress("Futoška 31");
        ana.setCity("Novi Sad");
        ana.setPhoneNumber("+381646789012");
        ana.getRoles().add(Role.ROLE_USER);
        userRepository.save(ana);
        attachAvatar(ana, "/uploads/avatars/ana.jpg");
        log.info("Regular user created: {} ({})", ana.getName(), ana.getEmail());
    }

    private void attachAvatar(User user, String path) {
        try {
            Image image = new Image();
            image.setPath(path);
            image.setUser(user);
            imageRepository.save(image);
        } catch (Exception ex) {
            log.warn("Failed to attach avatar for {} ({}): {}", user.getName(), user.getEmail(), ex.getMessage());
        }
    }
}

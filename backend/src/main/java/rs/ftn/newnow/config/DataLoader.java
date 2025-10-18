package rs.ftn.newnow.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rs.ftn.newnow.model.*;
import rs.ftn.newnow.model.enums.Role;
import rs.ftn.newnow.repository.*;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final AdministratorRepository administratorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (administratorRepository.count() == 0) {
            log.info("Initializing database with default data...");
            createAdministrators();
            createRegularUsers();
            log.info("Database initialization completed!");
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
        log.info("Administrator created: {} ({})", anja.getName(), anja.getEmail());

        // Admin 1: Marko Gordic
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
        log.info("Administrator created: {} ({})", marko.getName(), marko.getEmail());
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
        log.info("Regular user created: {} ({})", ana.getName(), ana.getEmail());
    }
}

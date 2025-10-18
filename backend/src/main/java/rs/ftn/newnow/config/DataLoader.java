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
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (administratorRepository.count() == 0) {
            createDefaultAdministrator();
        }
    }

    private void createDefaultAdministrator() {
        Administrator admin = new Administrator();
        admin.setEmail("admin@newnow.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setName("System Administrator");
        admin.setCreatedAt(LocalDate.now());
        admin.setAddress("Admin Address");
        admin.setCity("Novi Sad");
        admin.getRoles().add(Role.ROLE_ADMIN);
        admin.getRoles().add(Role.ROLE_USER);
        
        administratorRepository.save(admin);
        log.info("Default administrator created: {}", admin.getEmail());
    }
}

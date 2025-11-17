package kz.gov.rfs.config;

import kz.gov.rfs.entity.Role;
import kz.gov.rfs.entity.User;
import kz.gov.rfs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.init.username}")
    private String adminUsername;

    @Value("${admin.init.password}")
    private String adminPassword;

    @Value("${admin.init.email}")
    private String adminEmail;

    @Value("${admin.init.full-name}")
    private String adminFullName;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            log.info("Creating default admin user...");

            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setFullName(adminFullName);
            admin.setRole(Role.ADMIN);
            admin.setIsActive(true);
            admin.setIsEmailVerified(true);

            userRepository.save(admin);

            log.info("Default admin user created successfully!");
            log.info("Username: {}", adminUsername);
            log.info("Email: {}", adminEmail);
            log.info("Password: [HIDDEN - check your .env file]");
        } else {
            log.info("Admin user already exists, skipping initialization");
        }
    }
}
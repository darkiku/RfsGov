package kz.gov.rfs.config;

import kz.gov.rfs.entity.Role;
import kz.gov.rfs.entity.User;
import kz.gov.rfs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

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
    public void run(String... args) throws Exception {
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setFullName(adminFullName);
            admin.setRole(Role.ADMIN);
            admin.setIsActive(true);
            admin.setIsEmailVerified(true);

            userRepository.save(admin);

            System.out.println("========================================");
            System.out.println("ADMIN USER CREATED!");
            System.out.println("Username: " + adminUsername);
            System.out.println("Password: " + adminPassword);
            System.out.println("Email: " + adminEmail);
            System.out.println("PLEASE CHANGE PASSWORD AFTER FIRST LOGIN!");
            System.out.println("========================================");
        }
    }
}
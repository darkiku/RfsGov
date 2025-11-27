package kz.gov.rfs.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1) // Запускается первым
public class EnvironmentValidator implements CommandLineRunner {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${admin.init.password}")
    private String adminPassword;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;

    @Value("${spring.jpa.show-sql}")
    private boolean showSql;

    @Override
    public void run(String... args) {
        log.info("🔍 Validating environment configuration...");

        boolean hasErrors = false;

        // Проверка JWT_SECRET
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            log.error("❌ JWT_SECRET is not set");
            hasErrors = true;
        } else if (jwtSecret.length() < 64) {
            log.error("❌ JWT_SECRET is too short (minimum 64 characters). Current length: {}", jwtSecret.length());
            hasErrors = true;
        } else if (jwtSecret.equals("your-secret-key-here") || jwtSecret.equals("changeme")) {
            log.error("❌ JWT_SECRET must be changed from default value");
            hasErrors = true;
        } else {
            log.info("✅ JWT_SECRET configured correctly ({} characters)", jwtSecret.length());
        }

        // Проверка DB_PASSWORD
        if (dbPassword == null || dbPassword.trim().isEmpty()) {
            log.error("❌ DB_PASSWORD is not set");
            hasErrors = true;
        } else if (dbPassword.length() < 8) {
            log.warn("⚠️ DB_PASSWORD is too short (recommended minimum 12 characters)");
        } else {
            log.info("✅ DB_PASSWORD configured");
        }

        // Проверка ADMIN_PASSWORD
        if (adminPassword == null || adminPassword.trim().isEmpty()) {
            log.error("❌ ADMIN_PASSWORD is not set");
            hasErrors = true;
        } else if (adminPassword.length() < 8) {
            log.error("❌ ADMIN_PASSWORD is too short (minimum 8 characters)");
            hasErrors = true;
        } else if (adminPassword.equals("admin") || adminPassword.equals("password") || adminPassword.equals("123456")) {
            log.error("❌ ADMIN_PASSWORD is too weak. Use a strong password.");
            hasErrors = true;
        } else {
            log.info("✅ ADMIN_PASSWORD configured");
        }

        // Проверка режима Hibernate DDL
        if ("create".equals(ddlAuto) || "create-drop".equals(ddlAuto)) {
            log.error("❌ CRITICAL: hibernate.ddl-auto is set to '{}'. This will DELETE ALL DATA!", ddlAuto);
            log.error("❌ For production, use 'validate' or 'none'");
            hasErrors = true;
        } else if ("update".equals(ddlAuto)) {
            log.warn("⚠️ WARNING: hibernate.ddl-auto is set to 'update'. For production, consider using 'validate'");
        } else {
            log.info("✅ Hibernate DDL mode: {}", ddlAuto);
        }

        // Проверка SQL логирования
        if (showSql) {
            log.warn("⚠️ WARNING: spring.jpa.show-sql is enabled. This may expose sensitive data in logs");
        } else {
            log.info("✅ SQL logging disabled");
        }

        if (hasErrors) {
            log.error("❌❌❌ CONFIGURATION ERRORS DETECTED ❌❌❌");
            log.error("Please fix the errors above before running in production");
            throw new IllegalStateException("Critical configuration errors detected. Check logs above.");
        }

        log.info("✅ All environment variables validated successfully");
    }
}
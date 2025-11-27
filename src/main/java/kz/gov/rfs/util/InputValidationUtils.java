package kz.gov.rfs.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;


@Slf4j
@Component
public class InputValidationUtils {

    // Паттерны для валидации
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_-]{3,50}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[0-9]{10,15}$"
    );

    // Опасные SQL символы
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "('|(\\-\\-)|(;)|(\\|\\|)|(\\*)|(<)|(>)|(\\^)|(\\[)|(\\])|(\\{)|(\\})|(%)|(\\$))",
            Pattern.CASE_INSENSITIVE
    );

    // XSS паттерны
    private static final Pattern XSS_PATTERN = Pattern.compile(
            "(<script>|</script>|<iframe>|</iframe>|javascript:|onerror=|onload=)",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Проверка email
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Проверка username
     */
    public boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Проверка телефона
     */
    public boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Проверка на SQL Injection
     */
    public boolean containsSqlInjection(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }

    /**
     * Проверка на XSS
     */
    public boolean containsXss(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        return XSS_PATTERN.matcher(input).find();
    }

    /**
     * Санитизация строки от опасных символов
     */
    public String sanitize(String input) {
        if (input == null) {
            return null;
        }

        // Удаление HTML тегов
        String sanitized = input.replaceAll("<[^>]*>", "");

        // Удаление SQL опасных символов
        sanitized = sanitized.replaceAll("['\"\\-;]", "");

        // Удаление лишних пробелов
        sanitized = sanitized.trim();

        return sanitized;
    }

    /**
     * Валидация поискового запроса
     */
    public String validateSearchKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword cannot be empty");
        }

        if (keyword.length() > 100) {
            throw new IllegalArgumentException("Search keyword too long (max 100 characters)");
        }

        if (containsSqlInjection(keyword)) {
            log.warn("🚨 SECURITY: SQL Injection attempt detected in search: {}", keyword);
            throw new SecurityException("Invalid search query");
        }

        if (containsXss(keyword)) {
            log.warn("🚨 SECURITY: XSS attempt detected in search: {}", keyword);
            throw new SecurityException("Invalid search query");
        }

        return keyword.trim();
    }

    /**
     * Валидация ID параметра
     */
    public void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid ID parameter");
        }

        // Проверка на слишком большие значения (возможная атака)
        if (id > Long.MAX_VALUE - 1000) {
            throw new IllegalArgumentException("ID parameter out of valid range");
        }
    }

    /**
     * Валидация пагинации
     */
    public void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }

        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
    }
}
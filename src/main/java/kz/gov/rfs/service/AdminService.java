package kz.gov.rfs.service;

import kz.gov.rfs.dto.DashboardStats;
import kz.gov.rfs.entity.Role;
import kz.gov.rfs.entity.User;
import kz.gov.rfs.repository.RefreshTokenRepository;
import kz.gov.rfs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final NewsService newsService;
    private final ServiceService serviceService;
    private final ProcurementService procurementService;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setIsActive(true);
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        user.setFullName(userDetails.getFullName());

        // Проверка на уникальность email при изменении
        if (!user.getEmail().equals(userDetails.getEmail())
                && userRepository.existsByEmail(userDetails.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        user.setEmail(userDetails.getEmail());
        user.setRole(userDetails.getRole());
        user.setIsActive(userDetails.getIsActive());

        return userRepository.save(user);
    }

    @Transactional
    public User updateUserRole(Long id, Role role) {
        User user = getUserById(id);
        user.setRole(role);
        return userRepository.save(user);
    }

    @Transactional
    public User toggleUserStatus(Long id) {
        User user = getUserById(id);
        user.setIsActive(!user.getIsActive());

        // ИСПРАВЛЕНИЕ: при деактивации пользователя удаляем его refresh token
        if (!user.getIsActive()) {
            int deletedCount = refreshTokenRepository.deleteByUserId(user.getId());
            if (deletedCount > 0) {
                log.info("Deleted {} refresh token(s) for deactivated user: {}", deletedCount, user.getUsername());
            }
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);

        // ИСПРАВЛЕНИЕ: перед удалением пользователя удаляем его refresh token
        int deletedCount = refreshTokenRepository.deleteByUserId(user.getId());
        if (deletedCount > 0) {
            log.info("Deleted {} refresh token(s) for user being deleted: {}", deletedCount, user.getUsername());
        }

        userRepository.deleteById(id);
    }

    @Transactional
    public User changePassword(Long id, String newPassword) {
        log.info("Attempting to change password for user ID: {}", id);

        // Валидация пароля
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }

        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("Пароль должен содержать минимум 8 символов");
        }

        // КРИТИЧЕСКАЯ ВАЛИДАЦИЯ: пароль должен содержать буквы И цифры
        if (!newPassword.matches("^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$")) {
            throw new IllegalArgumentException("Пароль должен содержать минимум 8 символов, включая буквы и цифры");
        }

        User user = getUserById(id);

        // Хешируем новый пароль
        String newPasswordHash = passwordEncoder.encode(newPassword);
        user.setPassword(newPasswordHash);

        User savedUser = userRepository.save(user);

        // ИСПРАВЛЕНИЕ: после смены пароля удаляем все refresh токены ТОЛЬКО этого пользователя
        // НЕ ТРОГАЕМ токены текущего админа!
        int deletedCount = refreshTokenRepository.deleteByUserId(user.getId());
        if (deletedCount > 0) {
            log.info("Deleted {} refresh token(s) for user after password change: {}", deletedCount, user.getUsername());
        }

        log.info("Password changed successfully for user: {}", user.getUsername());

        return savedUser;
    }

    public DashboardStats getDashboardStats() {
        return DashboardStats.builder()
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.countByIsActive(true))
                .totalNews(newsService.getNewsCount())
                .totalServices(serviceService.getServiceCount())
                .totalProcurements(procurementService.getProcurementCount())
                .build();
    }
}
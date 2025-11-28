package kz.gov.rfs.service;

import kz.gov.rfs.dto.AuthRequest;
import kz.gov.rfs.dto.AuthResponse;
import kz.gov.rfs.entity.RefreshToken;
import kz.gov.rfs.entity.User;
import kz.gov.rfs.repository.RefreshTokenRepository;
import kz.gov.rfs.repository.UserRepository;
import kz.gov.rfs.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService;

    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenDuration;

    private final Map<String, Integer> loginAttempts = new ConcurrentHashMap<>();
    private final Map<String, Instant> lockoutTime = new ConcurrentHashMap<>();

    private static final int MAX_ATTEMPTS = 10;
    private static final long LOCKOUT_DURATION_MINUTES = 5;

    @Transactional
    public AuthResponse login(AuthRequest request) {
        String username = request.getUsername();
        checkLoginAttempts(username);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword())
            );

            User user = (User) authentication.getPrincipal();

            if (!user.getIsActive()) {
                throw new LockedException("Account is locked");
            }

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            String accessToken = jwtUtil.generateToken(authentication);

            log.info("🔑 Creating refresh token for user: {}", username);
            String refreshToken = createRefreshTokenSeparate(user.getId());
            log.info("✅ Refresh token created successfully");

            loginAttempts.remove(username);
            lockoutTime.remove(username);

            auditLogService.log(user, "LOGIN", "User", user.getId(), "User logged in successfully");
            log.info("User {} logged in successfully", username);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();

        } catch (BadCredentialsException e) {
            handleFailedLogin(username);
            log.warn("Invalid credentials for user: {}", username);
            throw new BadCredentialsException("Invalid username or password");
        } catch (LockedException e) {
            log.warn("Account locked: {}", username);
            throw e;
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user {}: {}", username, e.getMessage());
            throw e;
        }
    }

    private void checkLoginAttempts(String username) {
        if (lockoutTime.containsKey(username)) {
            Instant lockout = lockoutTime.get(username);
            if (Instant.now().isBefore(lockout)) {
                long secondsLeft = (lockout.toEpochMilli() - Instant.now().toEpochMilli()) / 1000;
                log.warn("Account {} is locked. Seconds left: {}", username, secondsLeft);
                throw new LockedException(String.format(
                        "Too many failed attempts. Please try again in %d seconds", secondsLeft
                ));
            } else {
                log.info("Lockout expired for user: {}, clearing attempts", username);
                loginAttempts.remove(username);
                lockoutTime.remove(username);
            }
        }
    }

    private void handleFailedLogin(String username) {
        int attempts = loginAttempts.getOrDefault(username, 0) + 1;
        loginAttempts.put(username, attempts);
        log.warn("Failed login attempt {} for user {}", attempts, username);

        if (attempts >= MAX_ATTEMPTS) {
            lockoutTime.put(username, Instant.now().plusSeconds(LOCKOUT_DURATION_MINUTES * 60));
            loginAttempts.remove(username);
            log.warn("Account {} locked due to {} failed login attempts", username, MAX_ATTEMPTS);
        }
    }

    /**
     * КРИТИЧЕСКИ ВАЖНО: Создание токена в ОТДЕЛЬНОЙ транзакции
     * Это гарантирует, что токен сохранится даже если основная транзакция откатится
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String createRefreshTokenSeparate(Long userId) {
        log.info("📝 Starting token creation for user ID: {}", userId);

        // Удаляем старый токен если есть
        try {
            int deleted = refreshTokenRepository.deleteByUserId(userId);
            log.info("Deleted {} old tokens for user {}", deleted, userId);
        } catch (Exception e) {
            log.warn("Error deleting old token: {}", e.getMessage());
        }

        // Получаем свежего пользователя из базы
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Создаем новый токен
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plusMillis(refreshTokenDuration));

        RefreshToken saved = refreshTokenRepository.saveAndFlush(token);

        log.info("✅ Token saved with ID: {}, token: {}", saved.getId(), saved.getToken().substring(0, 8) + "...");

        return saved.getToken();
    }

    @Transactional
    public AuthResponse refreshToken(String token) {
        log.debug("Attempting to refresh token: {}", token.substring(0, Math.min(8, token.length())) + "...");

        // PESSIMISTIC LOCK: блокирует токен для других транзакций
        RefreshToken refreshToken = refreshTokenRepository.findByTokenWithLock(token)
                .orElseThrow(() -> {
                    log.error("Refresh token not found in database");
                    return new RuntimeException("Invalid refresh token");
                });

        log.debug("Found refresh token for user: {}, expires at: {}",
                refreshToken.getUser().getUsername(), refreshToken.getExpiryDate());

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            log.error("Refresh token expired for user: {}", refreshToken.getUser().getUsername());
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired. Please login again");
        }

        User user = refreshToken.getUser();

        if (!user.getIsActive()) {
            log.error("User account is disabled: {}", user.getUsername());
            throw new RuntimeException("Account is disabled");
        }

        String newAccessToken = jwtUtil.generateTokenFromUsername(user.getUsername());

        // Удаляем старый токен (уже заблокирован, никто другой не может его трогать)
        refreshTokenRepository.delete(refreshToken);
        refreshTokenRepository.flush();

        String newRefreshToken = createRefreshTokenSeparate(user.getId());

        log.info("Token refreshed successfully for user {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public void logout(String username) {
        log.info("Logout requested for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenRepository.findByUser(user).ifPresent(token -> {
            refreshTokenRepository.delete(token);
            log.debug("Deleted refresh token for user: {}", username);
        });

        loginAttempts.remove(username);
        lockoutTime.remove(username);

        auditLogService.log(user, "LOGOUT", "User", user.getId(), "User logged out");
        log.info("User {} logged out successfully", username);
    }

    public void clearLockout(String username) {
        loginAttempts.remove(username);
        lockoutTime.remove(username);
        log.info("Cleared lockout for user: {}", username);
    }
}
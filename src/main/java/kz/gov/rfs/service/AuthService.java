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
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MINUTES = 15;

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
            String refreshToken = createRefreshToken(user);

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
                long minutesLeft = (lockout.toEpochMilli() - Instant.now().toEpochMilli()) / 60000;
                log.warn("Account {} is locked. Minutes left: {}", username, minutesLeft);
                throw new LockedException("Account locked. Try again in " + minutesLeft + " minutes");
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

    @Transactional
    public String createRefreshToken(User user) {
        try {
            int deletedCount = refreshTokenRepository.deleteByUserId(user.getId());
            if (deletedCount > 0) {
                log.debug("Deleted {} old refresh token(s) for user: {}", deletedCount, user.getUsername());
            }
        } catch (Exception e) {
            log.warn("Error deleting old refresh tokens for user {}: {}", user.getUsername(), e.getMessage());
        }

        // Создаем новый токен
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDuration));

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.debug("Created new refresh token for user: {}, expires at: {}",
                user.getUsername(), savedToken.getExpiryDate());

        return savedToken.getToken();
    }

    @Transactional
    public AuthResponse refreshToken(String token) {
        log.debug("Attempting to refresh token: {}", token.substring(0, Math.min(8, token.length())) + "...");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
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

        refreshTokenRepository.delete(refreshToken);
        String newRefreshToken = createRefreshToken(user);

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

        int deletedCount = refreshTokenRepository.deleteByUserId(user.getId());
        if (deletedCount > 0) {
            log.debug("Deleted {} refresh token(s) for user: {}", deletedCount, username);
        }

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
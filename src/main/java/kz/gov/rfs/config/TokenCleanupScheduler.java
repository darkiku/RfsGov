package kz.gov.rfs.config;

import kz.gov.rfs.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Запуск каждый день в 3:00 AM
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("🧹 Starting cleanup of expired refresh tokens...");

        try {
            long deletedCount = refreshTokenRepository.deleteAllExpiredTokens(Instant.now());
            log.info("✅ Cleaned up {} expired refresh tokens", deletedCount);
        } catch (Exception e) {
            log.error("❌ Error during token cleanup", e);
        }
    }

    @Scheduled(fixedRate = 21600000) // 6 часов в миллисекундах
    @Transactional
    public void periodicCleanup() {
        log.debug("🔄 Periodic token cleanup started");

        try {
            long deletedCount = refreshTokenRepository.deleteAllExpiredTokens(Instant.now());
            if (deletedCount > 0) {
                log.info("🧹 Periodic cleanup: removed {} expired tokens", deletedCount);
            }
        } catch (Exception e) {
            log.error("❌ Error during periodic cleanup", e);
        }
    }
}
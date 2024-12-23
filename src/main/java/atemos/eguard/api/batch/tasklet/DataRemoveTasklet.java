package atemos.eguard.api.batch.tasklet;

import atemos.eguard.api.repository.BlacklistedTokenRepository;
import atemos.eguard.api.repository.TwoFactorAuthRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * DataRemoveTasklet은 오래된 데이터를 주기적으로 삭제하는 작업을 처리하는 Tasklet입니다.
 * - 이 Tasklet은 BlacklistedToken, TwoFactorAuth 테이블에서 오래된 데이터를 삭제합니다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DataRemoveTasklet implements Tasklet {
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final TwoFactorAuthRepository twoFactorAuthRepository;

    /**
     * 오래된 데이터를 삭제하는 메서드입니다.
     * 이 메서드는 Spring Batch의 Tasklet에서 호출되어 배치 작업을 처리합니다.
     * - BlacklistedToken, TwoFactorAuth 테이블에서 오래된 데이터를 삭제합니다.
     *
     * @param contribution Step의 기여도 정보
     * @param chunkContext Chunk 관련 컨텍스트 정보
     * @return 작업 상태를 나타내는 RepeatStatus (FINISHED 반환 시 작업 완료)
     */
    @Override
    @Transactional
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) {
        // 현재 시간
        LocalDateTime now = LocalDateTime.now();
        // 1개월 전 시간 계산
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        // BlacklistedToken에서 1개월 이전의 데이터 삭제
        long deletedBlacklistedTokens = blacklistedTokenRepository.deleteByCreatedAtBefore(oneMonthAgo);
        logDeletion(deletedBlacklistedTokens, "BlacklistedToken", oneMonthAgo);
        // 1일 전 시간 계산
        LocalDateTime oneDayAgo = now.minusDays(1);
        // TwoFactorAuth에서 1일 이전의 데이터 삭제
        long deletedTwoFactorAuths = twoFactorAuthRepository.deleteByCreatedAtBefore(oneDayAgo);
        logDeletion(deletedTwoFactorAuths, "TwoFactorAuth", oneDayAgo);
        // 작업 완료 후 인증 정보 제거
        SecurityContextHolder.clearContext();
        return RepeatStatus.FINISHED;
    }

    /**
     * 삭제된 레코드 수가 0보다 큰 경우 로그를 출력하는 헬퍼 메서드입니다.
     *
     * @param count        삭제된 레코드 수
     * @param recordType   삭제된 레코드의 유형 (예: "energy", "AI forecast energy")
     * @param timeFrame    삭제 기준 기간
     */
    private void logDeletion(long count, String recordType, LocalDateTime timeFrame) {
        if (count > 0) {
            log.info("Deleted {} {} records older than {}.", count, recordType, timeFrame);
        }
    }
}
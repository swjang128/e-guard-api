package atemos.eguard.api.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * DataRemoveJobScheduler는 DataRemoveJob을 스케줄링하여 주기적으로 실행하는 클래스입니다.
 * - 매일 자정에 배치 작업을 실행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataRemoveJobScheduler {
    private final JobLauncher jobLauncher;
    private final Job dataRemoveJob;

    /**
     * 매일 자정에 DataRemoveJob을 실행합니다.
     * - JobParameters는 실행 시각을 포함하여 전달됩니다.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void runJob() throws Exception {
        log.info("**** [시작] 오래된 데이터 삭제.");
        // JobParametersBuilder에 현재 시간을 추가하여 배치 작업 실행
        jobLauncher.run(dataRemoveJob, new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters());
        log.info("**** [완료] 오래된 데이터 삭제.");
    }
}
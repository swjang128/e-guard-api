package atemos.eguard.api.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

/**
 * EventJobScheduler는 사건을 생성하는 EventJob을 실행하는 스케줄러 클래스입니다.
 * 이 클래스는 주기적으로 EventJob을 실행하여 사건 데이터를 처리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventJobScheduler {
    private final JobLauncher jobLauncher;
    private final Job eventJob;

    /**
     * EventJob을 실행하는 메서드입니다.
     * 이 메서드는 cron 표현식을 사용하여 5분마다 실행하도록 설정되었습니다.
     * 사건 생성 프로세스가 시작되면 시작 로그를 기록하고, 완료되면 완료 로그를 기록합니다.
     *
     * @throws Exception Job 실행 중 발생한 예외
     */
    //@Scheduled(cron = "5 * * * * *") // 테스트용
    //@Scheduled(cron = "0 */5 * * * *")
    public void runJob() throws Exception {
        log.info("**** [시작] 모든 구역과 근로자들의 상태를 확인하고 사건이 발생한 경우 기록.");
        // JobParametersBuilder를 통해 jobParameters 생성
        jobLauncher.run(eventJob, new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters());
        log.info("**** [완료] 모든 구역과 근로자들의 상태를 확인하고 사건이 발생한 경우 기록.");
    }
}
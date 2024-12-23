package atemos.eguard.api.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * AlarmJobScheduler는 알람을 생성하는 AlarmJob을 실행하는 스케줄러 클래스입니다.
 * 이 클래스는 주기적으로 AlarmJob을 실행하여 알람 데이터를 처리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmJobScheduler {
    private final JobLauncher jobLauncher;
    private final Job alarmJob;

    /**
     * AlarmJob을 실행하는 메서드입니다.
     * 이 메서드는 cron 표현식을 사용하여 매 5분 30초에 실행되도록 설정되었습니다.
     * 알람 생성 프로세스가 시작되면 시작 로그를 기록하고, 완료되면 완료 로그를 기록합니다.
     *
     * @throws Exception Job 실행 중 발생한 예외
     */
    @Scheduled(cron = "30 * * * * *") // 테스트용
    //@Scheduled(cron = "30 */5 * * * *")
    public void runJob() throws Exception {
        log.info("**** [시작] 사고가 발생한 공장의 근로자들에게 알람 생성.");
        // JobParametersBuilder를 통해 jobParameters 생성
        jobLauncher.run(alarmJob, new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters());
        log.info("**** [완료] 사고가 발생한 공장의 근로자들에게 알람 생성.");
    }
}
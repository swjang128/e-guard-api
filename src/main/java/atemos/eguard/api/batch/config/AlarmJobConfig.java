package atemos.eguard.api.batch.config;

import atemos.eguard.api.batch.tasklet.AlarmTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * AlarmJobConfig는 AlarmTasklet을 실행하는 Batch Job을 설정하는 클래스입니다.
 * 해당 Job은 Spring Batch의 Job, Step으로 구성됩니다.
 */
@Configuration
@RequiredArgsConstructor
public class AlarmJobConfig {
    /**
     * alarmJob 메서드는 AlarmTasklet을 실행하는 Batch Job을 구성합니다.
     *
     * @param jobRepository  JobRepository 객체 (Batch 메타데이터 저장)
     * @param alarmStep      AlarmTasklet을 포함하는 Step 객체
     * @return AlarmTasklet을 실행하는 Job 객체
     */
    @Bean
    public Job alarmJob(JobRepository jobRepository, Step alarmStep) {
        return new JobBuilder("alarmJob", jobRepository)
                .start(alarmStep)
                .build();
    }

    /**
     * alarmStep 메서드는 AlarmTasklet을 실행하는 Step을 구성합니다.
     *
     * @param jobRepository         JobRepository 객체
     * @param transactionManager    트랜잭션 매니저
     * @param alarmTasklet          알람 생성 로직을 담은 Tasklet 객체
     * @return AlarmTasklet을 실행하는 Step 객체
     */
    @Bean
    public Step alarmStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, AlarmTasklet alarmTasklet) {
        return new StepBuilder("alarmStep", jobRepository)
                .tasklet(alarmTasklet, transactionManager)
                .build();
    }
}
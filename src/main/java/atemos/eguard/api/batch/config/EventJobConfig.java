package atemos.eguard.api.batch.config;

import atemos.eguard.api.batch.tasklet.EventTasklet;
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
 * EventJobConfig는 EventTasklet을 실행하는 Batch Job을 설정하는 클래스입니다.
 * 해당 Job은 Spring Batch의 Job, Step으로 구성됩니다.
 */
@Configuration
@RequiredArgsConstructor
public class EventJobConfig {
    /**
     * eventJob 메서드는 EventTasklet을 실행하는 Batch Job을 구성합니다.
     *
     * @param jobRepository  JobRepository 객체 (Batch 메타데이터 저장)
     * @param eventStep      EventTasklet을 포함하는 Step 객체
     * @return EventTasklet을 실행하는 Job 객체
     */
    @Bean
    public Job eventJob(JobRepository jobRepository, Step eventStep) {
        return new JobBuilder("eventJob", jobRepository)
                .start(eventStep)
                .build();
    }

    /**
     * eventStep 메서드는 EventTasklet을 실행하는 Step을 구성합니다.
     *
     * @param jobRepository         JobRepository 객체
     * @param transactionManager    트랜잭션 매니저
     * @param eventTasklet          사건 생성 로직을 담은 Tasklet 객체
     * @return EventTasklet을 실행하는 Step 객체
     */
    @Bean
    public Step eventStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, EventTasklet eventTasklet) {
        return new StepBuilder("eventStep", jobRepository)
                .tasklet(eventTasklet, transactionManager)
                .build();
    }
}
package atemos.eguard.api.batch.config;

import atemos.eguard.api.batch.tasklet.DataRemoveTasklet;
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
 * DataRemoveJobConfig는 오래된 데이터를 삭제하는 작업을 Spring Batch로 구성하는 설정 클래스입니다.
 * - Job과 Step을 정의하여 주기적으로 Tasklet을 실행합니다.
 */
@Configuration
@RequiredArgsConstructor
public class DataRemoveJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    /**
     * 오래된 데이터를 삭제하는 배치 작업(Job)을 정의합니다.
     * - 이 Job은 DataRemoveTasklet을 실행하는 단일 Step으로 구성됩니다.
     *
     * @param dataRemoveStep DataRemoveTasklet을 실행하는 Step
     * @return Job 객체
     */
    @Bean
    public Job dataRemoveJob(Step dataRemoveStep) {
        return new JobBuilder("dataRemoveJob", jobRepository)
                .start(dataRemoveStep)
                .build();
    }

    /**
     * 오래된 데이터를 삭제하는 작업을 처리하는 Step을 정의합니다.
     * - 이 Step은 DataRemoveTasklet을 실행하여 데이터 삭제 작업을 처리합니다.
     *
     * @param dataRemoveTasklet 오래된 데이터를 삭제하는 Tasklet
     * @return Step 객체
     */
    @Bean
    public Step dataRemoveStep(DataRemoveTasklet dataRemoveTasklet) {
        return new StepBuilder("dataRemoveStep", jobRepository)
                .tasklet(dataRemoveTasklet, transactionManager)
                .build();
    }
}
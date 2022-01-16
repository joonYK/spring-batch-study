package jy.study.springBatch.flow;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.job.DefaultJobParametersExtractor;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 스텝의 순서를 외부화하는 세번째 방법.
 * 스텝을 전혀 외부화하지 않고 플로우를 작성하는 대신에 잡 내에서 다른 잡을 호출한다.
 * 잡 스텝은 외부 잡을 호출하는 스텝용 JobExecutionContext를 생성한다.
 *
 * 주의사항!
 * 실행 처리를 제어하는데 매우 큰 제약이 있을 수 있음.
 * 배치를 중지하거나, 잡 실행을 건너뛰어야할 수도 있는데, 잡 관리 기능은 단일 잡 수준에서 이뤄지기 때문에
 * 여러 잡으로 트리를 나눠서 작업하는것은 관리에 문제가 있을 수 있다.
 */
@EnableBatchProcessing
@Configuration
public class JobStepConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Tasklet loadStockFile() {
        return ((contribution, chunkContext) -> {
            System.out.println("The stock file has been loaded");
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Tasklet loadCustomerFile() {
        return ((contribution, chunkContext) -> {
            System.out.println("The customer file has been loaded");
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Tasklet updateStart() {
        return ((contribution, chunkContext) -> {
            System.out.println("The start has been updated");
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Tasklet runBatchTasklet() {
        return ((contribution, chunkContext) -> {
            System.out.println("The batch has been run");
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Job conditionalStepLogicJob() {
        return this.jobBuilderFactory.get("conditionalStepLogicJob3")
                //플로우를 래핑한 스텝을 잡 빌더에 전달.
                //해당 플로우가 담긴 스텝을 하나의 스텝처럼 기록하기 때문에 모니터링과 리포팅에 이점이 있음.
                //개별 스텝을 집계하지 않고도 플로우의 영향을 전체적으로 볼 수 있음.
                .start(initializeBatch())
                .next(runBatch())
                .build();
    }

    /**
     * 서브 잡.
     * 다른 잡과 마찬가지로 JobRepository 내에서 식별됨.
     * 자체적으로 JobInstance, ExecutionContext 및 관련 DB 레코드를 가진다.
     */
    @Bean
    public Job preProcessingJob() {
        return this.jobBuilderFactory.get("preProcessingJob")
                .start(loadFileStep())
                .next(loadCustomerStep())
                .next(updateStartStep())
                .build();
    }

    @Bean
    public Step initializeBatch() {
        return this.stepBuilderFactory.get("initializeBatch")
                .job(preProcessingJob())
                //상위 잡의 JobParameters 또는 ExecutionContext에서 파라미터를 추출해 하위 잡으로 전달하는 클래스를 정의.
                //서브 잡인 preProcessingJob에 상위 잡인 conditionalStepLogicJob3이 파라미터를 직접 전달하지 않음.
                .parametersExtractor(new DefaultJobParametersExtractor())
                .build();
    }

    @Bean
    public Step loadFileStep() {
        return this.stepBuilderFactory.get("loadFileStep")
                .tasklet(loadStockFile())
                .build();
    }

    @Bean
    public Step loadCustomerStep() {
        return this.stepBuilderFactory.get("loadCustomerStep")
                .tasklet(loadCustomerFile())
                .build();
    }

    @Bean
    public Step updateStartStep() {
        return this.stepBuilderFactory.get("updateStartStep")
                .tasklet(updateStart())
                .build();
    }

    @Bean
    public Step runBatch() {
        return this.stepBuilderFactory.get("runBatch")
                .tasklet(runBatchTasklet())
                .build();
    }

}

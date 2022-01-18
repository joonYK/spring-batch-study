package jy.study.springBatch.quartz;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class QuartzJobConfig {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job quartzJob() {
        return this.jobBuilderFactory.get("quartzJob")
                .incrementer(new RunIdIncrementer())
                .start(quartzStep())
                .build();
    }

    @Bean
    public Step quartzStep() {
        return this.stepBuilderFactory.get("quartzStep")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("quartzStep run!");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}

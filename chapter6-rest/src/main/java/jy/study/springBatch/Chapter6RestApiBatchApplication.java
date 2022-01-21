package jy.study.springBatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class Chapter6RestApiBatchApplication {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job restApiJob() {
        return this.jobBuilderFactory.get("restApiJob")
                .incrementer(new RunIdIncrementer())
                .start(step())
                .build();
    }

    @Bean
    public Step step() {
        return this.stepBuilderFactory.get("restApiStep")
                .tasklet(((contribution, chunkContext) -> {
                    System.out.println("restApiStep run!");
                    return RepeatStatus.FINISHED;
                })).build();
    }

    public static void main(String[] args) {
        SpringApplication.run(Chapter6RestApiBatchApplication.class, args);
    }
}

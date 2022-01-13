package jy.study.springBatch.conditional;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.util.Random;

//@EnableBatchProcessing
//@Configuration
public class ConditionalRandomJobConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Tasklet passTasklet() {
        return ((contribution, chunkContext) -> RepeatStatus.FINISHED);
    }

    @Bean
    public Tasklet successTasklet() {
        return ((contribution, chunkContext) -> {
            System.out.println("Success!");
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Tasklet failTasklet() {
        return ((contribution, chunkContext) -> {
            System.out.println("Failure!");
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("conditionalRandomJob")
                .start(firstStep())
                .next(decider())
                .from(decider())
                .on("FAILED").to(failureStep())
                .from(decider())
                .on("*").to(successStep())
                .end()
                .build();
    }


    @Bean
    public Step firstStep() {
        return this.stepBuilderFactory.get("firstStep")
                .tasklet(passTasklet())
                .build();
    }


    @Bean
    public Step successStep() {
        return this.stepBuilderFactory.get("successStep")
                .tasklet(successTasklet())
                .build();
    }


    @Bean
    public Step failureStep() {
        return this.stepBuilderFactory.get("failureStep")
                .tasklet(failTasklet())
                .build();
    }

    @Bean
    public JobExecutionDecider decider() {
        return new RandomDecider();
    }

    /**
     * JobExecutionDecider를 구현해서 다음에 무엇을 수행할지 프로그래밍적으로 결정
     */
    public static class RandomDecider implements JobExecutionDecider {

        private final Random random = new Random();

        @Override
        public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
            if (random.nextBoolean()) {
                return new FlowExecutionStatus(FlowExecutionStatus.COMPLETED.getName());
            } else {
                return new FlowExecutionStatus(FlowExecutionStatus.FAILED.getName());
            }
        }
    }
}

package jy.study.springBatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class JobExplorerConfig {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final JobExplorer jobExplorer;

    @Bean
    public Tasklet explorerTasklet() {
        return new ExploringTasklet(this.jobExplorer);
    }

    @Bean
    public Step explorerStep() {
        return this.stepBuilderFactory.get("explorerStep")
                .tasklet(explorerTasklet())
                .build();
    }

    @Bean
    public Job explorerJob() {
        return this.jobBuilderFactory.get("explorerJob")
                .start(explorerStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @RequiredArgsConstructor
    public static class ExploringTasklet implements Tasklet {

        private final JobExplorer explorer;

        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
            String jobName = chunkContext.getStepContext().getJobName();

            List<JobInstance> instances = explorer.getJobInstances(jobName, 0, Integer.MAX_VALUE);

            System.out.println(
                    String.format("There are %d job Instances for the job %s",
                    instances.size(),
                    jobName));

            System.out.println("They have had the following results");
            System.out.println("***********************************");

            for (JobInstance instance : instances) {
                List<JobExecution> jobExecutions = this.explorer.getJobExecutions(instance);

                System.out.println(
                        String.format("Instance %d had %d executions",
                        instance.getInstanceId(),
                        jobExecutions.size()));

                for (JobExecution jobExecution : jobExecutions) {
                    System.out.println(
                            String.format("\tExecution %d resulted in Exit Status %s",
                            jobExecution.getId(),
                            jobExecution.getExitStatus()));
                }
            }

            return RepeatStatus.FINISHED;
        }
    }


}

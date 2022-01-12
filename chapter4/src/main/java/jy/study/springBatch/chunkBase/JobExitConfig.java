package jy.study.springBatch.chunkBase;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 성공, 실패, 중지상태로 잡을 종료하기
 * 성공(Completed) : 해당 JobInstance는 동일한 파라미터로 다시 실행 불가.
 * 실패(Failed) : 동일한 파라미터로 다시 실행 가능.
 * 중지(Stopped) : 중단된 위치에서 잡을 다시 시작 가능.
 */
@EnableBatchProcessing
@Configuration
public class JobExitConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Tasklet passTasklet() {
        return ((contribution, chunkContext) -> {
            //return RepeatStatus.FINISHED;
            throw new RuntimeException("fail!!");
        });
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
        return this.jobBuilderFactory.get("exitJob")
                .start(firstStep())
                //스텝 실행 결과는 실패했지만 잡을 성공 상태로 저장.
                //.on("FAILED").end()

                //스텝 실행 결과 실패에 따라 잡도 실패 상태로 저장. 동일 파라미터로 재시작 가능.
                //.on("FAILED").fail()

                //스텝 실행 결과 실패에 따라 잡을 중지 상태로 저장. 동일 파라미터로 재시작 가능하며, 사용자가 미리 구성해둔 스텝부터 시작.
                .on("FAILED").stopAndRestart(successStep())
                .from(firstStep()).on("*").to(successStep())
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

}

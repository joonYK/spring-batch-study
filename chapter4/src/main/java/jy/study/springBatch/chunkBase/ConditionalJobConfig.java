package jy.study.springBatch.chunkBase;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ExitStatus로 잡의 진행 방향을 지정.
 */
@EnableBatchProcessing
@Configuration
public class ConditionalJobConfig {

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

    /**
     * on 메서드는 스프링 배치가 ExitStatus를 평가해 어떤 일을 수행할지 결정할 수 있도록 구성.
     * firstStep의 종료 코드가 FAILED면 failureStep 스텝으로 이동, FAILED가 아니면 successStep로 이동.
     */
    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("conditionalJob")
                .start(firstStep())
                .on("FAILED").to(failureStep())
                //와일드 카드 *는 0개 이상의 문자를 일치. ex) C* => C, COMPLETE, CORRECT
                //와일드 카드 ?는 1개의 문자를 일치. ex) ?AT => CAT, KAT
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

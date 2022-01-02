package jy.study.springBatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

//배치 잡 수행에 필요한 인프라스트럭처 제공
@EnableBatchProcessing
@SpringBootApplication
public class HelloWorldJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("basicJob")
                .start(step1())
                .build();
    }

//    @Bean
//    public Step step1() {
//        return this.stepBuilderFactory.get("step1")
//                //StepContribution : 아직 커밋되지 않은 트랜잭션에 대한 정보(쓰기 수, 읽기 수 등)
//                //ChunkContext : 실행 시점의 잡 상태 제공
//                .tasklet((contribution, chunkContext) -> {
//                    System.out.println("Hello, World!");
//                    return RepeatStatus.FINISHED;
//                }).build();
//    }

    @Bean
    public Step step1() {
        return this.stepBuilderFactory.get("step1")
                .tasklet(helloWorldTasklet())
                .build();
    }

    private Tasklet helloWorldTasklet() {
        return ((contribution, chunkContext) -> {
            //JobParameters 참조
            String name = (String) chunkContext.getStepContext()
                    .getJobParameters()
                    .get("name");
            System.out.printf("Hello, %s!%n", name);
            return RepeatStatus.FINISHED;
        });
    }

    public static void main(String[] args) {
        SpringApplication.run(HelloWorldJob.class, args);
    }

}

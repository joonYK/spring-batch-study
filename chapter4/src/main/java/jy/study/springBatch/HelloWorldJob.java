package jy.study.springBatch;

import jy.study.springBatch.incrementer.DailyJobTimestamper;
import jy.study.springBatch.validator.ParameterValidator;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

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
                .validator(compositeJobParametersValidator())
                .incrementer(new DailyJobTimestamper())
                .build();
    }

    @Bean
    public Step step1() {
        return this.stepBuilderFactory.get("step1")
                .tasklet(helloWorldTasklet(null))
                .build();
    }

    /*
     * 늦은 바인딩
     * 스텝의 실행 범위에 들어갈 때까지 빈 생성을 지연
     * 명령행 또는 다른 소스에서 받아들인 잡 파라미터를 빈 생성 시점에 주입
     */
    @StepScope
    @Bean
    public Tasklet helloWorldTasklet(
            @Value("#{jobParameters['name']}") String name) {

        return ((contribution, chunkContext) -> {
            System.out.printf("Hello, %s!%n", name);
            return RepeatStatus.FINISHED;
        });
    }

    /*
     * JobBuilder의 유효성 검증이 구성에는 하나의 인스턴스만 지정이 가능하기 때문에,
     * 여러 개의 유효성 검증기를 사용할 수 있게 제공
     */
    @Bean
    public CompositeJobParametersValidator compositeJobParametersValidator() {
        CompositeJobParametersValidator compositeValidator = new CompositeJobParametersValidator();
        DefaultJobParametersValidator validator = defaultJobParametersValidator();
        validator.afterPropertiesSet();

        compositeValidator.setValidators(
                Arrays.asList(new ParameterValidator(), validator)
        );

        return compositeValidator;
    }

    /*
     * 스프링 배치에서 제공하는 유효성 검증기
     */
    private DefaultJobParametersValidator defaultJobParametersValidator() {
        DefaultJobParametersValidator validator = new DefaultJobParametersValidator();
        //필수 파라미터 목록
        validator.setRequiredKeys(new String[] {"fileName"});
        //필수가 아닌 파라미터 목록
        //incrementer로 DailyJobTimestamper를 사용함에 따라 currentDate 추가
        validator.setOptionalKeys(new String[] {"name", "currentDate"});
        return validator;
    }

    public static void main(String[] args) {
        SpringApplication.run(HelloWorldJob.class, args);
    }

}

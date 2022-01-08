package jy.study.springBatch.differentTasklet;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing
@Configuration
public class MethodInvokingTaskletConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job methodInvokingJob() {
        return this.jobBuilderFactory.get("methodInvokingJob")
                .start(methodInvokingStep())
                .build();
    }

    @Bean
    public Step methodInvokingStep() {
        return this.stepBuilderFactory.get("methodInvokingStep")
                .tasklet(methodInvokingTasklet(null))
                .build();
    }

    @StepScope
    @Bean
    public Tasklet methodInvokingTasklet(
            @Value("#{jobParameters['message']}") String message
    ) {

        // 기존에 존재하던 다른 클래스 내의 메서드를 잡 내의 태스크릿처럼 사용 가능.
        MethodInvokingTaskletAdapter methodInvokingTaskletAdapter = new MethodInvokingTaskletAdapter();

        methodInvokingTaskletAdapter.setTargetObject(service());
        methodInvokingTaskletAdapter.setTargetMethod("serviceMethod");
        methodInvokingTaskletAdapter.setArguments(new String[] {message});

        return methodInvokingTaskletAdapter;
    }

    @Bean
    public CustomService service() {
        return new CustomService();
    }

    public static class CustomService {
        public void serviceMethod(String message) {
            System.out.println(message);
        }
    }
}

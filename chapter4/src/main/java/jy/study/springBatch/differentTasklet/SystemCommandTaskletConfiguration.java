package jy.study.springBatch.differentTasklet;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter;
import org.springframework.batch.core.step.tasklet.SystemCommandTasklet;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing
@Configuration
public class SystemCommandTaskletConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job systemCommandJob() {
        return this.jobBuilderFactory.get("systemCommandJob")
                .start(systemCommandStep())
                .build();
    }

    @Bean
    public Step systemCommandStep() {
        return this.stepBuilderFactory.get("systemCommandStep")
                .tasklet(systemCommandTasklet())
                .build();
    }

    @StepScope
    @Bean
    public Tasklet systemCommandTasklet() {
        //시스템 명령을 실행할 때 사용. 지정한 시스템 명령을 비동기로 실행.
        SystemCommandTasklet systemCommandTasklet = new SystemCommandTasklet();

        systemCommandTasklet.setCommand("rm -rf /tmp.txt");
        systemCommandTasklet.setTimeout(5000);

        //선택 사항. 잡이 비정상적으로 종료될 때 시스템 프로세스와 관련된 스레드를 강제 종료.
        systemCommandTasklet.setInterruptOnCancel(true);

        return systemCommandTasklet;
    }
}

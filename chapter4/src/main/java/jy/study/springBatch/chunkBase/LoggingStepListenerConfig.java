package jy.study.springBatch.chunkBase;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EnableBatchProcessing
@Configuration
public class LoggingStepListenerConfig {


    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job chunkBasedJob() {
        return this.jobBuilderFactory.get("stepListenerJob")
                .start(chunkStep())
                .build();
    }

    @Bean
    public Step chunkStep() {
        return this.stepBuilderFactory.get("stepListenerStep")
                .<String, String>chunk(10000)
                .reader(itemReader())
                .writer(itemWriter())
                .listener(new LoggingStepStartStropListener())
                .build();
    }

    @Bean
    public ListItemReader<String> itemReader() {
        List<String> items = new ArrayList<>(100000);

        for (int i = 0; i < 100000; i++)
            items.add(UUID.randomUUID().toString());

        return new ListItemReader<>(items);
    }

    @Bean
    public ItemWriter<String> itemWriter() {
        return items -> {
            for (String item : items) {
                System.out.println(">> current item = " + item);
            }
        };
    }

    /**
     * 스텝의 시작 및 종료시에 로깅하는 리스너
     */
    public static class LoggingStepStartStropListener {

        @BeforeStep
        public void beforeStep(StepExecution stepExecution) {
            System.out.println(stepExecution.getStepName() + " has begun!");
        }

        @AfterStep
        public ExitStatus afterStep(StepExecution stepExecution) {
            System.out.println(stepExecution.getStepName() + " has ended!");
            return stepExecution.getExitStatus();
        }
    }
}

package jy.study.springBatch;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBatchTest
@ContextConfiguration(classes = {
        JobTests.BatchConfiguration.class,
        BatchAutoConfiguration.class
})
public class JobTests {

    @Autowired
    JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    public void test() throws Exception {
        JobExecution jobExecution = this.jobLauncherTestUtils.launchJob();

        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();

        assertEquals(BatchStatus.COMPLETED, stepExecution.getStatus());
        assertEquals(3, stepExecution.getReadCount());
        assertEquals(3, stepExecution.getWriteCount());
    }

    @Configuration
    @EnableBatchProcessing
    public static class BatchConfiguration {

        @Autowired
        JobBuilderFactory jobBuilderFactory;

        @Autowired
        StepBuilderFactory stepBuilderFactory;

        @Bean
        public ListItemReader<String> itemReader() {
            return new ListItemReader<>(Arrays.asList("foo", "bar", "baz"));
        }

        @Bean
        public ItemWriter<String> itemWriter() {
            return (list -> {
                list.forEach(System.out::println);
            });
        }

        @Bean
        public Step step() {
            return this.stepBuilderFactory.get("step")
                    .<String, String>chunk(10)
                    .reader(itemReader())
                    .writer(itemWriter())
                    .build();
        }

        @Bean
        public Job job() {
            return this.jobBuilderFactory.get("job")
                    .start(step())
                    .build();
        }

        @Bean
        public DataSource dataSource() {
            return new EmbeddedDatabaseBuilder().build();
        }
    }
}

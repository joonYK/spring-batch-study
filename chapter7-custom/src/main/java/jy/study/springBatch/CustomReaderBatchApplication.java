package jy.study.springBatch;

import jy.study.springBatch.domain.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.ParseException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.FileNotFoundException;

@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class CustomReaderBatchApplication {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public CustomerItemReader customerItemReader() {
        CustomerItemReader customerItemReader = new CustomerItemReader();
        customerItemReader.setName("customerItemReader");

        return customerItemReader;
    }

    @Bean
    public ItemWriter<Customer> itemWriter() {
        return items -> items.forEach(System.out::println);
    }

    @Bean
    public Step step() {
        return this.stepBuilderFactory.get("customReaderStep")
                .<Customer, Customer>chunk(10)
                .reader(customerItemReader())
                .writer(itemWriter())
                .faultTolerant()
                //해당 예외타입은 건너뛰지 않음.
                .noSkip(Exception.class)
                //해당 예외 타입은 레코드를 건너뜀.
                .skip(RuntimeException.class)
                //해당 예외 타입에 대해서는 총 10회까지만 건너뛸 수 있음.
                .skipLimit(10)
                .build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("customReaderJob")
                .start(step())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(CustomReaderBatchApplication.class, args);
    }
}

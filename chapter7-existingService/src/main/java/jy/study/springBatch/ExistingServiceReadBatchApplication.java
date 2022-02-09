package jy.study.springBatch;

import jy.study.springBatch.domain.Customer;
import jy.study.springBatch.domain.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 기존에 사용하던 특정 객체를 ItemReader로 사용.
 */
@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class ExistingServiceReadBatchApplication {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public ItemReaderAdapter<Customer> customerItemReader(CustomerService customerService) {
        ItemReaderAdapter<Customer> adapter = new ItemReaderAdapter<>();

        //기존의 서비스 지정.
        adapter.setTargetObject(customerService);
        //해당 서비스의 메서드 지정.
        adapter.setTargetMethod("getCustomer");

        return adapter;
    }

    @Bean
    public ItemWriter<Customer> itemWriter() {
        return items -> items.forEach(System.out::println);
    }

    @Bean
    public Step step() {
        return this.stepBuilderFactory.get("existingServiceReadStep")
                .<Customer, Customer>chunk(10)
                .reader(customerItemReader(null))
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("existingServiceReadJob")
                .start(step())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(ExistingServiceReadBatchApplication.class, args);
    }
}

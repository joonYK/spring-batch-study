package jy.study.springBatch;

import jy.study.springBatch.domain.Customer;
import jy.study.springBatch.domain.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;

import java.util.Collections;

@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class RepositoryReadBatchApplication {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    @StepScope
    public RepositoryItemReader<Customer> customerItemReader(
            CustomerRepository repository,
            @Value("#{jobParameters['city']}") String city
    ) {
        return new RepositoryItemReaderBuilder<Customer>()
                .name("customerItemReader")
                .arguments(Collections.singletonList(city))
                .methodName("findByCity")
                .repository(repository)
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .pageSize(10)
                .build();
    }

    @Bean
    public ItemWriter<Object> itemWriter() {
        return items -> items.forEach(System.out::println);
    }

    @Bean
    public Step step() {
        return this.stepBuilderFactory.get("repositoryPagingReadStep")
                .<Customer, Customer>chunk(10)
                .reader(customerItemReader(null, null))
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("repositoryPagingReadJob")
                .start(step())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public static void main(String[] args) {

		SpringApplication.run(RepositoryReadBatchApplication.class, "city=Chicago");
    }
}

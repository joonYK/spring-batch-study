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
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class RepositoryWriterBatchApplication {


    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    @StepScope
    public FlatFileItemReader<Customer> customerItemReader(
            @Value("#{jobParameters['customerFile']}") Resource inputFile
    ) {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("customerItemReader")
                .delimited()
                .names("firstName", "middleInitial", "lastName", "address",
                        "city", "state", "zipCode")
                .targetType(Customer.class)
                .resource(inputFile)
                .build();
    }

    @Bean
    public RepositoryItemWriter<Customer> repositoryItemWriter(
            CustomerRepository repository
    ) {
        return new RepositoryItemWriterBuilder<Customer>()
                .repository(repository)
                .methodName("save")
                .build();
    }

    @Bean
    public Step step() {
        return this.stepBuilderFactory.get("repositoryWriteStep")
                .<Customer, Customer>chunk(10)
                .reader(customerItemReader(null))
                .writer(repositoryItemWriter(null))
                .build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("repositoryWriteJob")
                .start(step())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(RepositoryWriterBatchApplication.class, "customerFile=/input/customer.csv");
    }
}

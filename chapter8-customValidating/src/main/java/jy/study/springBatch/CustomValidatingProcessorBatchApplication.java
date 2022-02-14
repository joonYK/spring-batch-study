package jy.study.springBatch;

import jy.study.springBatch.domain.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class CustomValidatingProcessorBatchApplication {

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
    public ItemWriter<Object> itemWriter() {
        return items -> items.forEach(System.out::println);
    }
    

    @Bean
    public UniqueLastNameValidator validator() {
        UniqueLastNameValidator uniqueLastNameValidator = new UniqueLastNameValidator();
        uniqueLastNameValidator.setName("validator");
        return uniqueLastNameValidator;
    }

    @Bean
    public ValidatingItemProcessor<Customer> customerValidatingItemProcessor() {
        return new ValidatingItemProcessor<>(validator());
    }


    @Bean
    public Step step() {
        return this.stepBuilderFactory.get("customValidatingProcessorStep")
                .<Customer, Customer>chunk(5)
                .reader(customerItemReader(null))
                .processor(customerValidatingItemProcessor())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("customValidatingProcessorJob")
                .start(step())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public static void main(String[] args) {

		SpringApplication.run(CustomValidatingProcessorBatchApplication.class, "customerFile=/input/customer.csv");
    }
}

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
import org.springframework.batch.item.adapter.ItemProcessorAdapter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.ScriptItemProcessor;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import java.util.Arrays;

@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class CompositeProcessorBatchApplication {

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
    public UniqueLastNameValidator validator() {
        UniqueLastNameValidator uniqueLastNameValidator = new UniqueLastNameValidator();
        uniqueLastNameValidator.setName("validator");

        return uniqueLastNameValidator;
    }

    @Bean
    public ValidatingItemProcessor<Customer> customerValidatingItemProcessor() {
        ValidatingItemProcessor<Customer> itemProcessor = new ValidatingItemProcessor<>(validator());

        //유효성 검증을 통과하지 못한 아이템을 걸러내기만 하도록 구성.
        itemProcessor.setFilter(true);

        return itemProcessor;
    }

    @Bean
    public ItemProcessorAdapter<Customer, Customer> upperCaseItemProcessor(UpperCaseNameService service) {
        ItemProcessorAdapter<Customer, Customer> adapter = new ItemProcessorAdapter<>();

        adapter.setTargetObject(service);
        adapter.setTargetMethod("upperCase");

        return adapter;
    }

    @Bean
    @StepScope
    public ScriptItemProcessor<Customer, Customer> lowerCaseItemProcessor(
            @Value("#{jobParameters['script']}") Resource script
    ) {
        ScriptItemProcessor<Customer, Customer> itemProcessor = new ScriptItemProcessor<>();
        itemProcessor.setScript(script);

        return itemProcessor;
    }

    @Bean
    public CompositeItemProcessor<Customer, Customer> itemProcessor() {
        CompositeItemProcessor<Customer, Customer> itemProcessor = new CompositeItemProcessor<>();

        itemProcessor.setDelegates(Arrays.asList(
                customerValidatingItemProcessor(),
                upperCaseItemProcessor(null),
                lowerCaseItemProcessor(null)
        ));

        return itemProcessor;
    }

    @Bean
    public ItemWriter<Object> itemWriter() {
        return items -> items.forEach(System.out::println);
    }

    @Bean
    public Step step() {
        return this.stepBuilderFactory.get("compositeProcessorStep")
                .<Customer, Customer>chunk(5)
                .reader(customerItemReader(null))
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("compositeProcessorJob")
                .start(step())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public static void main(String[] args) {

		SpringApplication.run(CompositeProcessorBatchApplication.class,
                "customerFile=/input/customer.csv",
                "script=/script/lowerCase.js");
    }
}

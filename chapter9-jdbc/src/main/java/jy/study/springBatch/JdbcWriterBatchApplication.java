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
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class JdbcWriterBatchApplication {

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

    /**
     * JdbcBatchItemWriter PreparedStatement 표기법
     */
//    @Bean
//    @StepScope
//    public JdbcBatchItemWriter<Customer> jdbcCustomerWriter(DataSource dataSource) {
//        return new JdbcBatchItemWriterBuilder<Customer>()
//                .dataSource(dataSource)
//                .sql("INSERT INTO CUSTOMER (" +
//                        "first_name, " +
//                        "middle_initial, " +
//                        "last_name, " +
//                        "address, " +
//                        "city, " +
//                        "state, " +
//                        "zipcode) VALUES (?, ?, ?, ?, ?, ?, ?)")
//                .itemPreparedStatementSetter(new CustomerItemPreparedStatementSetter())
//                .build();
//    }

    /**
     * JdbcBatchItemWriter named parameter 표기법
     */
    @Bean
    @StepScope
    public JdbcBatchItemWriter<Customer> jdbcCustomerWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Customer>()
                .dataSource(dataSource)
                .sql("INSERT INTO CUSTOMER (" +
                        "first_name, " +
                        "middle_initial, " +
                        "last_name, " +
                        "address, " +
                        "city, " +
                        "state, " +
                        "zipcode) VALUES (" +
                        ":firstName, " +
                        ":middleInitial, " +
                        ":lastName, " +
                        ":address, " +
                        ":city, " +
                        ":state, " +
                        "zipCode)")
                .beanMapped()
                .build();
    }

    @Bean
    public Step step() {
        return this.stepBuilderFactory.get("jdbcWriteStep")
                .<Customer, Customer>chunk(5)
                .reader(customerItemReader(null))
                .writer(jdbcCustomerWriter(null))
                .build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("jdbcWriteJob")
                .start(step())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(JdbcWriterBatchApplication.class, "customerFile=/input/customer.csv");
    }
}

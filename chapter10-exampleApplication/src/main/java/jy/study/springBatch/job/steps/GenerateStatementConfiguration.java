package jy.study.springBatch.job.steps;

import jy.study.springBatch.domain.Statement;
import jy.study.springBatch.domain.customer.Customer;
import jy.study.springBatch.itemProcessor.AccountItemProcessor;
import jy.study.springBatch.itemWriter.StatementHeaherCallback;
import jy.study.springBatch.itemWriter.StatementLineAggregator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemWriter;
import org.springframework.batch.item.file.builder.MultiResourceItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class GenerateStatementConfiguration {

    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step generateStatements(AccountItemProcessor itemProcessor) {
        return this.stepBuilderFactory.get("generateStatements")
                //거래명세서마다 단일 파일로 생성되기때문에 청크 크기는 1
                .<Statement, Statement>chunk(1)
                .reader(statementItemReader(null))
                .processor(itemProcessor)
                .writer(statementItemWriter(null))
                .build();
    }


    @Bean
    public JdbcCursorItemReader<Statement> statementItemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Statement>()
                .name("statementItemReader")
                .dataSource(dataSource)
                .sql("SELECT * FROM CUSTOMER")
                .rowMapper((rs, rowNum) -> {
                    Customer customer = new Customer(
                            rs.getLong("customer_id"),
                            rs.getString("first_name"),
                            rs.getString("middle_name"),
                            rs.getString("last_name"),
                            rs.getString("address1"),
                            rs.getString("address2"),
                            rs.getString("city"),
                            rs.getString("state"),
                            rs.getString("postal_code"),
                            rs.getString("ssn"),
                            rs.getString("email_address"),
                            rs.getString("home_phone"),
                            rs.getString("cell_phone"),
                            rs.getString("work_phone"),
                            rs.getInt("notification_pref"));

                    return new Statement(customer);
                })
                .build();
    }

    @Bean
    public FlatFileItemWriter<Statement> individualStatementItemWriter() {
        FlatFileItemWriter<Statement> itemWriter = new FlatFileItemWriter<>();

        itemWriter.setName("individualStatementItemWriter");
        itemWriter.setHeaderCallback(new StatementHeaherCallback());
        itemWriter.setLineAggregator(new StatementLineAggregator());

        return itemWriter;
    }

    @Bean
    @StepScope
    public MultiResourceItemWriter<Statement> statementItemWriter(
            @Value("#{jobParameters['outputDirectory']}") Resource outputDir
    ) {
        return new MultiResourceItemWriterBuilder<Statement>()
                .name("statementItemWriter")
                .resource(outputDir)
                .itemCountLimitPerResource(1)
                .delegate(individualStatementItemWriter())
                .build();
    }
}

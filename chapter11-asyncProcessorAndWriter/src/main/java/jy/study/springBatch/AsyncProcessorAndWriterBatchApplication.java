package jy.study.springBatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.sql.DataSource;

/**
 * ItemProcessor에서 병목 현상이 있는 경우 AsyncItemProcessor를 사용해 새 스레드에서 동작시킴.
 * 결과로 반환되는 Future가 AsyncItemWriter로 전달됨.
 *
 * 반드시 AsyncItemProcessor와 AsyncItemWriter를 함께 사용해야함.
 */
@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class AsyncProcessorAndWriterBatchApplication {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    @StepScope
    public FlatFileItemReader<Transaction> fileTransactionReader(
            @Value("#{jobParameters['inputFlatFile']}") Resource resource
    ) {
        return new FlatFileItemReaderBuilder<Transaction>()
                .name("flatFileTransactionReader")
                .resource(resource)
                .delimited()
                .names("account", "amount", "timestamp")
                .fieldSetMapper(fieldSet -> {
                    Transaction transaction = new Transaction();

                    transaction.setAccount(fieldSet.readString("account"));
                    transaction.setAmount(fieldSet.readBigDecimal("amount"));
                    transaction.setTimestamp(fieldSet.readDate("timestamp", "yyyy-MM-dd HH:mm:ss"));

                    return transaction;
                })
                .build();
    }

    @Bean
    public AsyncItemProcessor<Transaction, Transaction> asyncItemProcessor() {
        AsyncItemProcessor<Transaction, Transaction> processor = new AsyncItemProcessor<>();

        processor.setDelegate(processor());
        processor.setTaskExecutor(new SimpleAsyncTaskExecutor());

        return processor;
    }

    @Bean
    public ItemProcessor<Transaction, Transaction> processor() {
        return transaction -> {
            Thread.sleep(5);
            return transaction;
        };
    }

    @Bean
    public JdbcBatchItemWriter<Transaction> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaction>()
                .dataSource(dataSource)
                .beanMapped()
                .sql("INSERT INTO TRANSACTION (ACCOUNT, AMOUNT, TIMESTAMP) " +
                        "VALUES (:account, :amount, :timestamp)")
                .build();
    }

    @Bean
    public AsyncItemWriter<Transaction> asyncItemWriter() {
        AsyncItemWriter<Transaction> writer = new AsyncItemWriter<>();
        writer.setDelegate(writer(null));

        return writer;
    }

    @Bean
    public Step step1Async() {
        return this.stepBuilderFactory.get("step1Async")
                .<Transaction, Transaction>chunk(100)
                .reader(fileTransactionReader(null))
                .processor((ItemProcessor) asyncItemProcessor())
                .writer(asyncItemWriter())
                .build();
    }

    @Bean
    public Job asyncJob() {
        return this.jobBuilderFactory.get("asyncJob")
                .start(step1Async())
                .incrementer(new RunIdIncrementer())
                .build();
    }


    public static void main(String[] args) {
        SpringApplication.run(AsyncProcessorAndWriterBatchApplication.class,
                "inputFlatFile=/input/bigTransactions.csv");
    }
}

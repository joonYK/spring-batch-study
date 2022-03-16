package jy.study.springBatch;

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
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.sql.DataSource;

@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class MultiThreadChunkBatchApplication {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    @StepScope
    public FlatFileItemReader<Transaction> fileTransactionReader(
            @Value("#{jobParameters['inputFlatFile']}") Resource resource
    ) {
        return new FlatFileItemReaderBuilder<Transaction>()
                .name("transactionItemReader")
                .resource(resource)
                /*
                 * 다중 스레드 환경에서 여러 스레드가 접근 가능한 상태를 가진 객체에는 서로 다른 스레드의 상태로 덮어 쓰는 문제가 발생.
                 * 리더의 상태가 저장(중단된 위치를 알 수 있는 상태)되지 않도록 해서 해당 잡을 재시작할 수 없게 한다.
                 */
                .saveState(false)
                .delimited()
                .names(new String[] {"account", "amount", "timestamp"})
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
    @StepScope
    public JdbcBatchItemWriter<Transaction> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaction>()
                .dataSource(dataSource)
                .sql("INSERT INTO TRANSACTION2 (ACCOUNT, AMOUNT, TIMESTAMP) " +
                        "VALUES(:account, :amount, :timestamp)")
                .beanMapped()
                .build();
    }

    @Bean
    public Step step() {
        return this.stepBuilderFactory.get("step")
                .<Transaction, Transaction>chunk(100)
                .reader(fileTransactionReader(null))
                .writer(writer(null))
                // 스텝에 TaskExecutor 구현체를 정의.
                // 스텝 내에서 실행되는 각 청크용으로 새 스레드를 생성해 각 청크를 병렬로 실행.
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public Job multithreadedJob() {
        return this.jobBuilderFactory.get("multithreadedChunkJob")
                .start(step())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(MultiThreadChunkBatchApplication.class, "inputFlatFile=/input/bigTransactions.csv");

    }
}

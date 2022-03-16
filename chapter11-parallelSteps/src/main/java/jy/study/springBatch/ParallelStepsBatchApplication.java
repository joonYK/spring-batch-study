package jy.study.springBatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.sql.DataSource;

/**
 * 모든 스텝을 병렬로 실행. ex) 서로 관련 없는 여러 파일을 가져옴.
 * 플로우(재사용 가능한 스텝의 묶음)도 동시에 실행 가능.
 */
@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class ParallelStepsBatchApplication {

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
    @StepScope
    public StaxEventItemReader<Transaction> xmlTransactionReader(
            @Value("#{jobParameters['inputXmlFile']}") Resource resource
    ) {
        Jaxb2Marshaller unmarshaller = new Jaxb2Marshaller();
        unmarshaller.setClassesToBeBound(Transaction.class);

        return new StaxEventItemReaderBuilder<Transaction>()
                .name("xmlFileTransactionReader")
                .resource(resource)
                .addFragmentRootElements("transaction")
                .unmarshaller(unmarshaller)
                .build();
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<Transaction> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaction>()
                .dataSource(dataSource)
                .beanMapped()
                .sql("INSERT INTO TRANSACTION (ACCOUNT, AMOUNT, TIMESTAMP) " +
                        "VALUES (:account, :amount, :timestamp)")
                .build();
    }

    @Bean
    public Step step1() {
        return this.stepBuilderFactory.get("step1")
                .<Transaction, Transaction>chunk(100)
                .reader(xmlTransactionReader(null))
                .writer(writer(null))
                .build();
    }

    @Bean
    public Step step2() {
        return this.stepBuilderFactory.get("step2")
                .<Transaction, Transaction>chunk(100)
                .reader(fileTransactionReader(null))
                .writer(writer(null))
                .build();
    }

    /**
     * 2개의 플로우를 만들어서 2개의 스텝을 병렬로 실행
     */
    @Bean
    public Job parallelStepJob() {
        Flow secondFlow = new FlowBuilder<Flow>("secondFlow")
                .start(step2())
                .build();

        Flow parallelFlow = new FlowBuilder<Flow>("parallelFlow")
                .start(step1())
                /*
                 * 여러 플로우를 병렬로 실행하려면 split 메서드를 사용해서 taskExecutor 구현체를 전달. (각 플로우는 자체 스레드에서 실행하게 됨)
                 * split 메서드를 통해 병렬로 수행되도록 구성된 여러 플로우가 모두 완료된 이후에 다음 스텝이 실행됨.
                 */
                .split(new SimpleAsyncTaskExecutor())
                .add(secondFlow)
                .build();

        return this.jobBuilderFactory.get("parallelStepsJob")
                .start(parallelFlow)
                .end()
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(ParallelStepsBatchApplication.class,
                "inputFlatFile=/input/bigTransactions.csv",
                "inputXmlFile=/input/bigTransactions.xml");

    }
}

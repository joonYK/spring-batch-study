package jy.study.springBatch.job.steps;

import jy.study.springBatch.domain.transaction.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class ImportTransactionConfiguration {

    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step importTransactions() {
        return this.stepBuilderFactory.get("importTransactions")
                .<Transaction, Transaction>chunk(100)
                .reader(transactionItemReader(null))
                .writer(transactionWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public StaxEventItemReader<Transaction> transactionItemReader(
            @Value("#{jobParameters['transactionFile']}") Resource transactionFile
    ) {
        Jaxb2Marshaller unmarshaller = new Jaxb2Marshaller();
        unmarshaller.setClassesToBeBound(Transaction.class);

        return new StaxEventItemReaderBuilder<Transaction>()
                .name("transactionReader")
                .resource(transactionFile)
                .addFragmentRootElements("transaction")
                .unmarshaller(unmarshaller)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Transaction> transactionWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaction>()
                .dataSource(dataSource)
                .sql("INSERT INTO TRANSACTION (" +
                        "TRANSACTION_ID, " +
                        "ACCOUNT_ACCOUNT_ID, " +
                        "DESCRIPTION, " +
                        "CREDIT, " +
                        "DEBIT, " +
                        "TIMESTAMP) VALUES (" +
                        ":transactionId, " +
                        ":accountId, " +
                        ":description, " +
                        ":credit, " +
                        ":debit, " +
                        ":timestamp)")
                .beanMapped()
                .build();
    }
}

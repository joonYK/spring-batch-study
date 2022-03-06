package jy.study.springBatch.job.steps;

import jy.study.springBatch.domain.transaction.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class ApplyTransactionConfiguration {

    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step applyTransactions() {
        return this.stepBuilderFactory.get("applyTransactions")
                .<Transaction, Transaction>chunk(100)
                .reader(applyTransactionReader(null))
                .writer(applyTransactionWriter(null))
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Transaction> applyTransactionReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Transaction>()
                .name("applyTransactionReader")
                .dataSource(dataSource)
                .sql("SELECT TRANSACTION_ID, " +
                        "ACCOUNT_ACCOUNT_ID, " +
                        "DESCRIPTION, " +
                        "CREDIT, " +
                        "DEBIT, " +
                        "TIMESTAMP " +
                        "FROM TRANSACTION " +
                        "ORDER BY TIMESTAMP")
                .rowMapper(((rs, rowNum) ->
                        new Transaction(
                                rs.getLong("transaction_id"),
                                rs.getLong("account_account_id"),
                                rs.getString("description"),
                                rs.getBigDecimal("credit"),
                                rs.getBigDecimal("debit"),
                                rs.getTimestamp("timestamp"))
                ))
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Transaction> applyTransactionWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaction>()
                .dataSource(dataSource)
                .sql("UPDATE ACCOUNT SET " +
                        "BALANCE = BALANCE + :transactionAmount " +
                        "WHERE ACCOUNT_ID = :accountId")
                .beanMapped()
                .assertUpdates(false)
                .build();
    }
}

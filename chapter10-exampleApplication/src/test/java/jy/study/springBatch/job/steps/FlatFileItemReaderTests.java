package jy.study.springBatch.job.steps;


import jy.study.springBatch.domain.customer.CustomerAddressUpdate;
import jy.study.springBatch.domain.customer.CustomerContactUpdate;
import jy.study.springBatch.domain.customer.CustomerNameUpdate;
import jy.study.springBatch.domain.customer.CustomerUpdate;
import jy.study.springBatch.itemProcessor.AccountItemProcessor;
import jy.study.springBatch.itemProcessor.CustomerItemValidator;
import jy.study.springBatch.itemWriter.CustomerUpdateClassifier;
import jy.study.springBatch.itemWriter.StatementHeaherCallback;
import jy.study.springBatch.itemWriter.StatementLineAggregator;
import jy.study.springBatch.job.ImportJobConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        CustomerItemValidator.class,
        StatementHeaherCallback.class,
        StatementLineAggregator.class,
        AccountItemProcessor.class,
        ImportCustomerUpdatesConfiguration.class,
        ImportTransactionConfiguration.class,
        GenerateStatementConfiguration.class,
        ApplyTransactionConfiguration.class,
        ImportJobConfiguration.class})
@JdbcTest
@EnableBatchProcessing
@SpringBatchTest
public class FlatFileItemReaderTests {

    @Autowired
    FlatFileItemReader<CustomerUpdate> customerUpdateItemReader;

    public StepExecution getStepExecution() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("customerUpdateFile", "classpath:customerUpdateFile.csv")
                .toJobParameters();

        return MetaDataInstanceFactory.createStepExecution(jobParameters);
    }

    @Test
    public void testTypeConversion() throws Exception {
        this.customerUpdateItemReader.open(new ExecutionContext());

        assertTrue(this.customerUpdateItemReader.read() instanceof CustomerAddressUpdate);
        assertTrue(this.customerUpdateItemReader.read() instanceof CustomerContactUpdate);
        assertTrue(this.customerUpdateItemReader.read() instanceof CustomerNameUpdate);
    }
}

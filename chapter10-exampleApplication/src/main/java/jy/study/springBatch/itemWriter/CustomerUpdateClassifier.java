package jy.study.springBatch.itemWriter;

import jy.study.springBatch.domain.customer.CustomerAddressUpdate;
import jy.study.springBatch.domain.customer.CustomerContactUpdate;
import jy.study.springBatch.domain.customer.CustomerNameUpdate;
import jy.study.springBatch.domain.customer.CustomerUpdate;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.classify.Classifier;

public class CustomerUpdateClassifier implements Classifier<CustomerUpdate, ItemWriter<? super CustomerUpdate>> {

    private final JdbcBatchItemWriter<CustomerUpdate> recordType1ItemWriter;
    private final JdbcBatchItemWriter<CustomerUpdate> recordType2ItemWriter;
    private final JdbcBatchItemWriter<CustomerUpdate> recordType3ItemWriter;

    public CustomerUpdateClassifier(
            JdbcBatchItemWriter<CustomerUpdate> recordType1ItemWriter,
            JdbcBatchItemWriter<CustomerUpdate> recordType2ItemWriter,
            JdbcBatchItemWriter<CustomerUpdate> recordType3ItemWriter
    ) {
        this.recordType1ItemWriter = recordType1ItemWriter;
        this.recordType2ItemWriter = recordType2ItemWriter;
        this.recordType3ItemWriter = recordType3ItemWriter;
    }

    @Override
    public ItemWriter<? super CustomerUpdate> classify(CustomerUpdate classifiable) {

        if (classifiable instanceof CustomerNameUpdate)
            return recordType1ItemWriter;
        else if (classifiable instanceof CustomerAddressUpdate)
            return recordType2ItemWriter;
        else if (classifiable instanceof CustomerContactUpdate)
            return recordType3ItemWriter;
        else
            throw new IllegalArgumentException("Invalid type: " +
                    classifiable.getClass().getCanonicalName());
    }
}

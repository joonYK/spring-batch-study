package jy.study.springBatch;

import jy.study.springBatch.domain.Customer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;

/**
 * 아이템을 입력으로 전달받고 해당 아이템에 대해 쓰기 작업을 수행할 ItemWriter를 반환하는 Classifier 구현체.
 */
public class CustomerClassifier implements Classifier<Customer, ItemWriter<? super Customer>> {

    private final ItemWriter<Customer> fileItemWriter;
    private final ItemWriter<Customer> jdbcItemWriter;

    public CustomerClassifier(ItemWriter<Customer> fileItemWriter, ItemWriter<Customer> jdbcItemWriter) {
        this.fileItemWriter = fileItemWriter;
        this.jdbcItemWriter = jdbcItemWriter;
    }

    @Override
    public ItemWriter<? super Customer> classify(Customer customer) {
        if (customer.getState().matches("^[A-M].*"))
            return fileItemWriter;
        else
            return jdbcItemWriter;
    }
}

package jy.study.springBatch;

import jy.study.springBatch.domain.Customer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.classify.Classifier;

/**
 * 입력 아이템에 따라 처리할 ItemProcessor 을 반환하는 Classifier 인터페이스.
 * 이 구현체는 우편번호가 홀수일지 짝수인지에 따라 ItemProcessor 반환.
 */
public class ZipCodeClassifier implements Classifier<Customer, ItemProcessor<Customer, Customer>> {

    private ItemProcessor<Customer, Customer> oddItemProcessor;
    private ItemProcessor<Customer, Customer> evenItemProcessor;

    public ZipCodeClassifier(
            ItemProcessor<Customer, Customer> oddItemProcessor,
            ItemProcessor<Customer, Customer> evenItemProcessor
    ) {
        this.oddItemProcessor = oddItemProcessor;
        this.evenItemProcessor = evenItemProcessor;
    }

    @Override
    public ItemProcessor<Customer, Customer> classify(Customer classifiable) {
        if (Integer.parseInt(classifiable.getZipCode()) % 2 == 0) {
            return evenItemProcessor;
        } else {
            return oddItemProcessor;
        }
    }
}

package jy.study.springBatch;

import jy.study.springBatch.domain.Customer;
import org.springframework.batch.item.ItemProcessor;

/**
 * ItemProcessor을 직접 구현.
 * 이 구현체는 우편번호가 짝수이면 null을 반환 (필터링 처리)
 */
public class EvenFilteringItemProcessor implements ItemProcessor<Customer, Customer> {

    @Override
    public Customer process(Customer item) throws Exception {
        return Integer.parseInt(item.getZipCode()) % 2 == 0 ? null : item;
    }
}

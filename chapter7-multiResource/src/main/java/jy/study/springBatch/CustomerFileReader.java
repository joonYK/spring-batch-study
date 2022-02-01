package jy.study.springBatch;

import jy.study.springBatch.domain.Customer;
import jy.study.springBatch.domain.Transaction;
import org.springframework.batch.item.*;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.core.io.Resource;

import java.util.ArrayList;

public class CustomerFileReader implements ResourceAwareItemReaderItemStream<Customer> {

    private Object curItem = null;

    private final ResourceAwareItemReaderItemStream<Object> delegate;

    public CustomerFileReader(ResourceAwareItemReaderItemStream<Object> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Customer read() throws Exception {
        if (curItem == null)
            curItem = delegate.read();

        //먼저 파일에서 고객 레코드르 읽어 들임
        Customer item = (Customer) curItem;
        curItem = null;

        if (item != null) {
            //거래내역 초기화
            item.setTransactions(new ArrayList<>());

            //다음 고객 레코드를 만나기전까지 거래내역 레코드를 한 줄씩 계속 읽어들임
            while (peek() instanceof Transaction) {
                item.getTransactions().add((Transaction) curItem);
                //레코드 처리 이후에 peek 메서드가 알 수 있도록 curItem에 null 설정
                curItem = null;
            }
        }

        return item;
    }

    /**
     * 현재 처리 중인 Customer를 처리하는 과정에서 레코드를 미리 읽어 놓는 데 사용.
     * 현재 레코드를 캐시(curItem)에 저장.
     */
    private Object peek() throws Exception {
        if (curItem == null)
            curItem = delegate.read();

        return curItem;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        delegate.open(executionContext);
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        delegate.update(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        delegate.close();
    }

    /**
     * ItemReader가 스스로 파일 관리를 하는 대신 필요한 파일을 주입해줄 수 있음.
     */
    @Override
    public void setResource(Resource resource) {
        this.delegate.setResource(resource);
    }
}

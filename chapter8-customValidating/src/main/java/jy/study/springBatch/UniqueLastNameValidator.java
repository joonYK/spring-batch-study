package jy.study.springBatch;

import jy.study.springBatch.domain.Customer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamSupport;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.Validator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * JobExecution 간에 상태를 저장할 수 있도록 ItemStreamSupport를 상속.
 * 유효성 검증기를 직접 구현하기위해 Validator 인터페이스 구현.
 */
public class UniqueLastNameValidator extends ItemStreamSupport implements Validator<Customer> {

    private Set<String> lastNames = new HashSet<>();

    @Override
    public void validate(Customer value) throws ValidationException {
        //현재 레코드의 lastName 값이 Set에 존재하지 않으면 추가.
        if (lastNames.contains(value.getLastName())) {
            throw new ValidationException("Duplicate last name was found: " + value.getLastName());
        }

        this.lastNames.add(value.getLastName());
    }

    /**
     * lastNames 필드가 이전 Execution에 저장돼 있는지 확인.
     * 만약 저장돼 있다면 스텝 처리가 시작되기 전에 해당 값으로 원복.
     */
    @Override
    public void open(ExecutionContext executionContext) {
        String lastNames = getExecutionContextKey("lastNames");

        if (executionContext.containsKey(lastNames)) {
            this.lastNames = (Set<String>) executionContext.get(lastNames);
        }
    }

    /**
     * 다음 청크에 오류가 발생할 경우 현재 상태를 executionContext에 저장.
     * 트랜잭션이 커밋되면 청크당 한 번 호출됨.
     */
    @Override
    public void update(ExecutionContext executionContext) {
        Iterator<String> itr = lastNames.iterator();
        Set<String> copiedLastNames = new HashSet<>();
        while (itr.hasNext())
            copiedLastNames.add(itr.next());

        executionContext.put(getExecutionContextKey("lastNames"), copiedLastNames);
    }
}

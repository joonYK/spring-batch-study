package jy.study.springBatch;

import jy.study.springBatch.domain.Customer;
import org.springframework.batch.item.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomerItemReader extends ItemStreamSupport implements ItemReader<Customer> {

    private List<Customer> customers;
    private int curIndex;
    private String INDEX_KEY = "current.index.customers";

    private String [] firstNames = {"Michael", "Warren", "Ann", "Terrence",
            "Erica", "Laura", "Steve", "Larry"};
    private String middleInitial = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private String [] lastNames = {"Gates", "Darrow", "Donnelly", "Jobs",
            "Buffett", "Ellison", "Obama"};
    private String [] streets = {"4th Street", "Wall Street", "Fifth Avenue",
            "Mt. Lee Drive", "Jeopardy Lane",
            "Infinite Loop Drive", "Farnam Street",
            "Isabella Ave", "S. Greenwood Ave"};
    private String [] cities = {"Chicago", "New York", "Hollywood", "Aurora",
            "Omaha", "Atherton"};
    private String [] states = {"IL", "NY", "CA", "NE"};

    private Random generator = new Random();

    public CustomerItemReader() {
        curIndex = 0;

        customers = new ArrayList<>();

        for(int i = 0; i < 100; i++) {
            customers.add(buildCustomer());
        }
    }

    private Customer buildCustomer() {
        Customer customer = new Customer();

        customer.setId((long) generator.nextInt(Integer.MAX_VALUE));
        customer.setFirstName(firstNames[generator.nextInt(firstNames.length - 1)]);
        customer.setMiddleInitial(
                String.valueOf(middleInitial.charAt(
                        generator.nextInt(middleInitial.length() - 1))));
        customer.setLastName(lastNames[generator.nextInt(lastNames.length - 1)]);
        customer.setAddress(generator.nextInt(9999) + " " +
                streets[generator.nextInt(states.length - 1)]);
        customer.setCity(cities[generator.nextInt(cities.length - 1)]);
        customer.setState(states[generator.nextInt(states.length - 1)]);
        customer.setZipCode(String.valueOf(generator.nextInt(99999)));

        return customer;
    }

    @Override
    public Customer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        Customer customer = null;

        //50번째 Customer 객체를 처리한 뒤에 잡을 강제로 종료.
        if (curIndex == 50) {
            curIndex++;
            throw new RuntimeException("This will end your execution");
        }

        if (curIndex < customers.size()) {
            customer = customers.get(curIndex);
            curIndex++;
        }

        return customer;
    }

    /**
     * close 메서드는 리소스를 닫는데 사용.
     */
    @Override
    public void close() {
    }

    /**
     * open 메서드는 ItemReader에서 필요한 상태를 초기화하려고 호출.
     * 초기화는 잡을 재시작할 때 이전 상태를 복원, 특정 파일을 열거나 DB에 연결하는 것.
     * ex) 처리된 레코드의 개수를 가져와서 다시 잡을 실행할 때 해당 레코드 숫자만큼 건너뜀.
     */
    @Override
    public void open(ExecutionContext executionContext) {
        //update 메서드에서 값을 설정했는지 여부를 체크
        if (executionContext.containsKey(getExecutionContextKey(INDEX_KEY))) {
            //값이 설정되어있으면 잡을 재시작하는것을 의미.

            /*
             * ExecutionContext에서 사용되는 키에 대한 참조가 ItemStreamSupport가 제공하는
             * getExecutionContextKey 메서드를 사용해서 전달됨.
             */
            int index = executionContext.getInt(getExecutionContextKey(INDEX_KEY));

            //복원하려는 인덱스가 50이면 run 메서드 내에 추가한 예외 코드 때문에 발생한 것으로 해당 레코드를 건너뜀.
            if (index == 50) {
                curIndex = 51;
            } else {
                curIndex = index;
            }
        } else {
            curIndex = 0;
        }
    }

    /**
     * update 메서드는 스프링 배치가 잡의 상태를 갱신하는 처리에 사용.
     * 얼마나 많은 레코드나 청크가 처리됐는지 기록.
     */
    @Override
    public void update(ExecutionContext executionContext) {
        //현재 처리 중인 레코드를 나타내는 키-값 쌍을 추가
        executionContext.putInt(getExecutionContextKey(INDEX_KEY), curIndex);
    }
}

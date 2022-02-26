package jy.study.springBatch.domain;

import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    public void logCustomer(Customer cust) {
        System.out.println("I just saved " + cust);
    }
}

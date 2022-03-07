package jy.study.springBatch.domain;

import jy.study.springBatch.domain.customer.Customer;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

//거래명세서
@Getter
public class Statement {

    //거래명세서의 주인인 고객 정보
    private final Customer customer;
    //고객이 가진 각 계좌 리스트
    private List<Account> accounts = new ArrayList<>();

    public Statement(Customer customer, List<Account> accounts) {
        this.customer = customer;
        this.accounts.addAll(accounts);
    }

    public Statement(Customer customer) {
        this.customer = customer;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts.addAll(accounts);
    }
}
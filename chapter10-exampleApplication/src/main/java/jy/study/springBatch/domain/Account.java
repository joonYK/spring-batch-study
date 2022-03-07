package jy.study.springBatch.domain;

import jy.study.springBatch.domain.transaction.Transaction;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Getter
public class Account {
    private long id;
    private BigDecimal balance;
    private Date lastStatementDate;
    private List<Transaction> transactions = new ArrayList<>();

    public Account(long id, BigDecimal balance, Date lastStatementDate) {
        this.id = id;
        this.balance = balance;
        this.lastStatementDate = lastStatementDate;
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
    }
}

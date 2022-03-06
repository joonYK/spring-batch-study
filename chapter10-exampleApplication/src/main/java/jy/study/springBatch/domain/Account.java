package jy.study.springBatch.domain;

import jy.study.springBatch.domain.transaction.Transaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Account {
    private long id;
    private BigDecimal balance;
    private Date lastStatementDate;
    private List<Transaction> transactions = new ArrayList<>();
}

package jy.study.springBatch.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Transaction {

    private long transactionId;

    private long accountId;

    private String description;

    private BigDecimal credit;

    private BigDecimal debit;

    private Date timestamp;

}

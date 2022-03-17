package jy.study.springBatch;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class Transaction {

    private String account;

    private BigDecimal amount;

    private Date timestamp;

}

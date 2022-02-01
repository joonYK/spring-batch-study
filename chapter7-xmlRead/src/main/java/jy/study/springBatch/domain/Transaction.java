package jy.study.springBatch.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlType;
import java.util.Date;

@Getter
@Setter
@ToString
@XmlType(name = "transaction")
public class Transaction {

    private String accountNumber;

    private Date transactionDate;

    private Double amount;
}

package jy.study.springBatch.domain.transaction;

import lombok.*;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@XmlRootElement(name = "transaction")
public class Transaction {

    private long transactionId;

    private long accountId;

    private String description;

    //입금액
    private BigDecimal credit;

    //출금액
    private BigDecimal debit;

    private Date timestamp;

    //String 을 java.util.Date 로 변환하기 위해 해당 애노테이션 적용.
    @XmlJavaTypeAdapter(JaxbDateSerializer.class)
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getTransactionAmount() {
        if (credit != null) {
            if (debit != null) {
                return credit.add(debit);
            } else {
                return credit;
            }
        } else if (debit != null) {
            return debit;
        } else {
            return new BigDecimal(0);
        }
    }

}

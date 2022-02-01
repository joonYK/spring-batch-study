package jy.study.springBatch.domain;

import lombok.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
//매칭되는 엘리먼트 지정
@XmlRootElement
public class Customer {

    //이름
    private String firstName;

    //가운데 이름의 첫 글자
    private String middleInitial;

    //성
    private String lastName;

    //건물 번호 + 거리 이름
    private String address;

    //거주 도시
    private String city;

    //CA(캘리포니아),TX(텍사스) 등 주의 두 자리 약자
    private String state;

    //우편번호
    private String zipCode;

    private List<Transaction> transactions;

    //파서에게 거래 내역
    // 구조를 알림
    @XmlElementWrapper(name = "transactions")
    @XmlElement(name = "transaction")
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}

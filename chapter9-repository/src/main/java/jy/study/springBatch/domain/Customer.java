package jy.study.springBatch.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "customer")
public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    private String zipcode;
}

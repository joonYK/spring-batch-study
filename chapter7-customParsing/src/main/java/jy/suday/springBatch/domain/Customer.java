package jy.suday.springBatch.domain;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Customer {

    //이름
    private String firstName;

    //가운데 이름의 첫 글자
    private String middleInitial;

    //성
    private String lastName;

    //주소에서 건물 번호 부분
    //private String addressNumber;

    //addressNumber + street
    private String address;

    //거주하는 거리 이름
	//private String street;

    //거주 도시
    private String city;

    //CA(캘리포니아),TX(텍사스) 등 주의 두 자리 약자
    private String state;

    //우편번호
    private String zipCode;

}

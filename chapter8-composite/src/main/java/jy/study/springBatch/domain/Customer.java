package jy.study.springBatch.domain;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Customer {

    private String firstName;

    private String middleInitial;

    private String lastName;

    private String address;

    private String city;

    private String state;

    private String zipCode;

    public Customer(Customer customer) {
        this.firstName = customer.firstName;
        this.middleInitial = customer.middleInitial;
        this.lastName = customer.lastName;
        this.address = customer.address;
        this.city = customer.city;
        this.state = customer.state;
        this.zipCode = customer.zipCode;
    }
}

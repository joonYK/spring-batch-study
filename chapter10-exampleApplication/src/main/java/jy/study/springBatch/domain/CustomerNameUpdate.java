package jy.study.springBatch.domain;

import lombok.Getter;

@Getter
public class CustomerNameUpdate extends CustomerUpdate {

    private final String firstName;

    private final String middleName;

    private final String lastName;

    public CustomerNameUpdate(long customerId, String firstName, String middleName, String lastName) {
        super(customerId);
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
    }
}

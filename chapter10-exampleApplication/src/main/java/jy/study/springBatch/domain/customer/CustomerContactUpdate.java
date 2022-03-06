package jy.study.springBatch.domain.customer;

import lombok.Getter;

@Getter
public class CustomerContactUpdate extends CustomerUpdate {

    private final String emailAddress;

    private final String homePhone;

    private final String cellPhone;

    private final String workPhone;

    private final Integer notificationPreferences;

    public CustomerContactUpdate(long customerId, String emailAddress, String homePhone, String cellPhone, String workPhone, Integer notificationPreferences) {
        super(customerId);
        this.emailAddress = emailAddress;
        this.homePhone = homePhone;
        this.cellPhone = cellPhone;
        this.workPhone = workPhone;
        this.notificationPreferences = notificationPreferences;
    }
}

package jy.study.springBatch.itemProcessor;

import jy.study.springBatch.domain.customer.Customer;
import jy.study.springBatch.domain.customer.CustomerUpdate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerItemValidatorTest {

    @Mock
    NamedParameterJdbcTemplate template;

    CustomerItemValidator validator;

    @BeforeEach
    public void setUp() {
        this.validator = new CustomerItemValidator(this.template);
    }

    @Test
    public void testValidCustomer() {
        //given
        CustomerUpdate customerUpdate = new CustomerUpdate(5L);

        //when
        ArgumentCaptor<Map<String, Long>> parameterMap = ArgumentCaptor.forClass(Map.class);

        when(this.template.queryForObject(
                    eq(CustomerItemValidator.FIND_CUSTOMER),
                    parameterMap.capture(),
                    eq(Long.class)
        )).thenReturn(2L);

        this.validator.validate(customerUpdate);

        //then
        assertEquals(5L, parameterMap.getValue().get("id"));
    }

    @Test
    public void testInvalidCustomer() {
        //given
        CustomerUpdate customerUpdate = new CustomerUpdate(5L);

        //when
        ArgumentCaptor<Map<String, Long>> parameterMap = ArgumentCaptor.forClass(Map.class);

        when(this.template.queryForObject(
                eq(CustomerItemValidator.FIND_CUSTOMER),
                parameterMap.capture(),
                eq(Long.class)
        )).thenReturn(0L);

        Throwable throwable = assertThrows(ValidationException.class,
                () -> this.validator.validate(customerUpdate));

        //then
        assertEquals("Customer id 5 was not able to be found", throwable.getMessage());
    }

}
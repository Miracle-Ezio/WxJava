package com.starry.mb.customer.dto;

import com.starry.mb.customer.domain.Customer;
import lombok.Data;

@Data
public class CustomerDetailVO {
    private Customer customer;
    private String consultantName;
}

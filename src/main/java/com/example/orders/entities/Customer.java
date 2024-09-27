package com.example.orders.entities;

import lombok.Data;

import java.util.Date;

@Data
public class Customer {
    String customerId;
    String email;
    Date createdDate;
}

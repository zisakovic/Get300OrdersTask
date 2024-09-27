package com.example.orders.entities;

import lombok.Data;

import java.util.List;

@Data
public class Order {
    int orderId;
    String invoiceCustomerId;
    List<OrderItem> items;
}

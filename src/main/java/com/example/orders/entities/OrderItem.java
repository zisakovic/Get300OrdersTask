package com.example.orders.entities;

import lombok.Data;

@Data
public class OrderItem {
    int position;
    String title;
    int price;
    int tax;
}

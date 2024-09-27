package com.example.orders.entities;

import lombok.Data;

import java.util.List;

@Data
public class Orders {
    int endingBeforeId;
    int startingAfterId;
    List<Order> items;
}

package com.example.orders.entities;

import lombok.Data;

import java.util.List;

@Data
public class Invoices {
    List<Invoice> items;
}

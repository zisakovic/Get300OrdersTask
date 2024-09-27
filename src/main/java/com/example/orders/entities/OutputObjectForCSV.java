package com.example.orders.entities;

import lombok.Data;

import java.util.Date;

@Data
public class OutputObjectForCSV {
    int orderId;
    int position;
    String title;
    int price;
    int tax;
    String customerId;
    String customerEmail;
    Date customerCreationDate;
    int invoiceId;
    Date invoiceDate;
}

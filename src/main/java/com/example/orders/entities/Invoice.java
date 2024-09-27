package com.example.orders.entities;

import lombok.Data;

import java.util.Date;

@Data
public class Invoice {
    int invoiceId;
    Date invoiceDate;
}

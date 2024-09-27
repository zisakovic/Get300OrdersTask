package com.example.orders;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class OrderCommands {

    @ShellMethod(key = "getFirst300Orders")
    public String getFirst300Orders() {
        return OrdersService.getFirst300Orders();
    }

    @ShellMethod(key = "getCSVFile")
    public String getCSVFile() {
        String csvFile = OrdersService.getCSVFile();
        return "CSV File: " + csvFile;
    }
}

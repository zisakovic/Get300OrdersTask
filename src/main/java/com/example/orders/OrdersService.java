package com.example.orders;

import com.example.orders.entities.*;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class OrdersService {

    private static final String PLENIGO_TOKEN_NAME = "X-plenigo-token";
    private static final String PLENIGO_TOKEN_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJBY2Nlc3NHcm91cHMiOiJBQ0NFU1NfUklHSFRTO0FDVElWSVRJRVM7QU5BTFlUSUNTO0FQUFNUT1JFUztBQ0NPVU5USU5HUztDQUxMQkFDS1M7Q0hFQ0tPVVQ7Q1VTVE9NRVJTO0RPV05MT0FEUztJTVBPUlRTO0lOVk9JQ0VTO01BSUxTO09SREVSUztQUk9DRVNTRVM7UFJPRFVDVFM7U1RBVFVTO1NVQlNDUklQVElPTlM7VFJBTlNBQ1RJT05TO1ZPVUNIRVJTO1dBTExFVFM7U0VUVElOR1M7U0ZUUCIsIkFjY2Vzc1JpZ2h0cyI6IkFQSV9SRUFEIiwiQ29tcGFueUlkIjoiUlc4OFZPTTNGOTlNSjYyWEJRVE0iLCJJZCI6IjJqZUZ3WlpVNFdHVzVNQVU5amMwQmNhb1NUMiJ9.6WvcZ09LYr5FfA2QaT1FCoojVv4OH6BhuqfwTfxkErc";

    private static final String ORDERS_URL = "https://api.plenigo-stage.com/api/v3.0/orders";
    private static final String CUSTOMERS_URL = "https://api.plenigo-stage.com/api/v3.0/customers";
    private static final String INVOICES_URL = "https://api.plenigo-stage.com/api/v3.0/invoices";

    private static RestTemplate restTemplate;

    public static String getFirst300Orders() {

        List<Order> orders = getOrders();
        List<Integer> orderIdList = orders.stream().map(Order::getOrderId).toList();
        return "Orders: " + orderIdList;
    }

    public static String getCSVFile() {

        String csvFileName = "orders.csv";

        try (Writer writer = new FileWriter(csvFileName)) {
            List<Order> orders = getOrders();

            List<OutputObjectForCSV> outputObjectForCSVList = new ArrayList<>();
            for (Order order: orders) {

                Customer customer = getCustomer(order);
                Invoices invoices = getInvoices(order);
                boolean populateInvoiceData = invoices != null
                        && invoices.getItems() != null
                        && !invoices.getItems().isEmpty();

                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    for (OrderItem orderItem: order.getItems()) {
                        OutputObjectForCSV outputObjectForCSV = new OutputObjectForCSV();
                        populateOrderData(order, outputObjectForCSV);
                        populateOrderItemData(orderItem, outputObjectForCSV);
                        populateCustomerData(outputObjectForCSV, customer);
                        if (populateInvoiceData) {
                            populateInvoiceData(outputObjectForCSV, invoices);
                        }
                        outputObjectForCSVList.add(outputObjectForCSV);
                    }
                } else {
                    OutputObjectForCSV outputObjectForCSV = new OutputObjectForCSV();
                    populateOrderData(order, outputObjectForCSV);
                    populateCustomerData(outputObjectForCSV, customer);
                    if (populateInvoiceData) {
                        populateInvoiceData(outputObjectForCSV, invoices);
                    }
                    outputObjectForCSVList.add(outputObjectForCSV);
                }
            }

            if (!outputObjectForCSVList.isEmpty()) {
                StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
                beanToCsv.write(outputObjectForCSVList);
            }
        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            csvFileName = "NO FILE";
        }
        return csvFileName;
    }

    private static RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            restTemplate = new RestTemplateBuilder()
                    .defaultHeader(PLENIGO_TOKEN_NAME, PLENIGO_TOKEN_KEY)
                    .build();
        }
        return restTemplate;
    }

    private static URI getOrdersUri(int startingAfter, int size) {
        UriComponents builder = UriComponentsBuilder.fromHttpUrl(ORDERS_URL)
                .queryParam("sort","ASC")
                .queryParam("startingAfter", startingAfter)
                .queryParam("size", size)
                .build();
        return builder.encode().toUri();
    }

    private static List<Order> getOrders() {
        List<Order> orders = new ArrayList<>();
        RestTemplate restTemplate = getRestTemplate();
        int startingAfter = 0;
        int size = 100;
        boolean retrievedOrdersLessThanPageSize = false;
        while (orders.size() < 300 && !retrievedOrdersLessThanPageSize) {
            Orders ordersOnCurrentPage = restTemplate.getForEntity(getOrdersUri(startingAfter, size), Orders.class).getBody();
            if (ordersOnCurrentPage != null) {
                orders.addAll(ordersOnCurrentPage.getItems());
                startingAfter = ordersOnCurrentPage.getStartingAfterId();
                if (ordersOnCurrentPage.getItems().size() < size) {
                    retrievedOrdersLessThanPageSize = true;
                }
            } else {
                retrievedOrdersLessThanPageSize = true;
            }
        }
        return orders;
    }

    private static Customer getCustomer(Order order) {
        String customerUrl = CUSTOMERS_URL + "/" + order.getInvoiceCustomerId();
        return getRestTemplate().getForEntity(customerUrl, Customer.class).getBody();
    }

    private static Invoices getInvoices(Order order) {
        String invoicesUrl = INVOICES_URL + "?orderId=" + order.getOrderId();
        return getRestTemplate().getForEntity(invoicesUrl, Invoices.class).getBody();
    }

    private static void populateOrderData(Order order, OutputObjectForCSV outputObjectForCSV) {
        outputObjectForCSV.setOrderId(order.getOrderId());
        outputObjectForCSV.setCustomerId(order.getInvoiceCustomerId());
    }

    private static void populateOrderItemData(OrderItem orderItem, OutputObjectForCSV outputObjectForCSV) {
        outputObjectForCSV.setPosition(orderItem.getPosition());
        outputObjectForCSV.setTitle(orderItem.getTitle());
        outputObjectForCSV.setPrice(orderItem.getPrice());
        outputObjectForCSV.setTax(orderItem.getTax());
    }

    private static void populateCustomerData(OutputObjectForCSV outputObjectForCSV, Customer customer) {
        outputObjectForCSV.setCustomerEmail(customer.getEmail());
        outputObjectForCSV.setCustomerCreationDate(customer.getCreatedDate());
    }

    private static void populateInvoiceData(OutputObjectForCSV outputObjectForCSV, Invoices invoices) {
        outputObjectForCSV.setInvoiceId(invoices.getItems().get(0).getInvoiceId());
        outputObjectForCSV.setInvoiceDate(invoices.getItems().get(0).getInvoiceDate());
    }
}

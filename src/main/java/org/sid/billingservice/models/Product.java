package org.sid.billingservice.models;

import lombok.Data;

@Data
public class Product {
    private long id;
    private String name;
    private double price;
    private double quantity;
    private boolean promotion;
}
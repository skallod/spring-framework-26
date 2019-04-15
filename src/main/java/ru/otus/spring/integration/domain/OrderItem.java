package ru.otus.spring.integration.domain;

public class OrderItem {

    private final String itemName;

    private final boolean iced;

    public OrderItem(String itemName, boolean iced) {
        this.itemName = itemName;
        this.iced = iced;
    }

    public String getItemName() {
        return itemName;
    }

    public boolean isIced() {
        return iced;
    }
}

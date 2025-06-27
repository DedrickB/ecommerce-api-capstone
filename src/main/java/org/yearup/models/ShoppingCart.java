package org.yearup.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ShoppingCart {
    private Map<Integer, ShoppingCartItem> items = new HashMap<>();

    @JsonProperty("items")
    public Map<Integer, ShoppingCartItem> getItems() {
        return items;
    }

    public void setItems(Map<Integer, ShoppingCartItem> items) {
        this.items = items;
    }

    @JsonProperty("total")
    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ShoppingCartItem item : items.values()) {
            total = total.add(item.getLineTotal());
        }
        return total;
    }


    public void add(ShoppingCartItem item) {
        this.items.put(item.getProduct().getProductId(), item);
    }
}
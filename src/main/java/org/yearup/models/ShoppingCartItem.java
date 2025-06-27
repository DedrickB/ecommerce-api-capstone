package org.yearup.models;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class ShoppingCartItem {
    private Product product;
    private int quantity = 1;
    private BigDecimal discountPercent = BigDecimal.ZERO;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }


    @JsonProperty("lineTotal")
    public BigDecimal getLineTotal() {
        BigDecimal basePrice = product.getPrice();
        BigDecimal quantityDecimal = new BigDecimal(this.quantity);
        return basePrice.multiply(quantityDecimal);
    }
}
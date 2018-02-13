package com.wx.invoicefx.model.entities.item;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 30.12.17.
 */
public class Vat {

    private final double value;
    private final int category;

    public Vat(double value, int category) {
        this.value = value;
        this.category = category;
    }

    public double getValue() {
        return value;
    }

    public int getCategory() {
        return category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vat vat = (Vat) o;

        if (Double.compare(vat.value, value) != 0) return false;
        return category == vat.category;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(value);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + category;
        return result;
    }

    @Override
    public String toString() {
        return "VAT{" +
                "value=" + value +
                ", category=" + category +
                '}';
    }
}

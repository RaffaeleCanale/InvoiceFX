package com.wx.invoicefx.model.entities.purchase;

import com.wx.invoicefx.model.entities.DateEnabled;
import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.item.Item;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 15.06.16.
 */
public class Purchase {

    private Item item;
    private int itemCount;
    private DateEnabled dateEnabled;
    private LocalDate fromDate;
    private LocalDate toDate;

    public double getSum() {
        return itemCount * item.getPrice();
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public DateEnabled getDateEnabled() {
        return dateEnabled;
    }

    public void setDateEnabled(DateEnabled dateEnabled) {
        this.dateEnabled = dateEnabled;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    @Override
    public String toString() {
        return getItemCount() + " * " + getItem() + " from " + getFromDate() + " to " + getToDate() + " | " + getDateEnabled().name();
    }
}

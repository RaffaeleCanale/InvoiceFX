package com.wx.invoicefx.model.entities.item;

import com.wx.invoicefx.model.entities.DateEnabled;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created on 02/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
@XmlRootElement
public class Item {

    public static Item copy(Item item) {
        Item copy = new Item();
        copy.setName(item.getName());
        copy.setPrice(item.getPrice());
        copy.setVat(item.getVat());
        copy.setDefaultDateEnabled(item.getDefaultDateEnabled());
        copy.setActive(item.isActive());

        return copy;
    }

    private long id;
    private String name;
    private double price;
    private Vat vat;
    private DateEnabled defaultDateEnabled;

    private boolean active;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Vat getVat() {
        return vat;
    }

    public void setVat(Vat vat) {
        this.vat = vat;
    }

    public DateEnabled getDefaultDateEnabled() {
        return defaultDateEnabled;
    }

    public void setDefaultDateEnabled(DateEnabled defaultDateEnabled) {
        this.defaultDateEnabled = defaultDateEnabled;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "[" + getId() + "] " + getName() + " / " + getPrice() + " / " + getVat() + "% / " + getDefaultDateEnabled();
    }
}

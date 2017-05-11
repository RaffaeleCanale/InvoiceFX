package com.wx.invoicefx.model.entities.client;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * A {@code ClientItem} models an item purchased by the client. Thus, it is defined by the item itself, but also the
 * amount, dates, client name,... Created on 03/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class Client {

    private final LongProperty id = new SimpleLongProperty();
    private final BooleanBinding idValidity = id.greaterThan(0);

    private final StringProperty name = new SimpleStringProperty();
    private final BooleanBinding nameValidity = name.isNotNull();

    //<editor-fold desc="Getters & Setters" defaultstate="collapsed">
    public long getId() {
        return id.get();
    }

    public LongProperty idProperty() {
        return id;
    }

    public void setId(long id) {
        this.id.set(id);
    }

    public BooleanBinding idValidityProperty() {
        return idValidity;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public BooleanBinding nameValidityProperty() {
        return nameValidity;
    }
    //</editor-fold>


    @Override
    public String toString() {
        return "[" + getId() + "] " + getName();
    }
}

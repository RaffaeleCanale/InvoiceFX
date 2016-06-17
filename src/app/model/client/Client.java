package app.model.client;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

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

    private final ObservableList<PurchasedItem> purchasedItems = FXCollections.observableArrayList();
    private final BooleanBinding purchasedItemsValidity = Bindings.isNotEmpty(purchasedItems);


    private final DoubleProperty sum = new SimpleDoubleProperty();

    public Client() {
        purchasedItems.addListener((ListChangeListener<PurchasedItem>) c -> recomputeTotal());
    }



    private void recomputeTotal() {
        sum.unbind();

        purchasedItems.stream()
                .map(PurchasedItem::sumProperty)
                .reduce(DoubleExpression::add)
                .ifPresent(sum::bind);
    }

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

    public ObservableList<PurchasedItem> getPurchasedItems() {
        return purchasedItems;
    }

    public BooleanBinding purchasedItemsValidityProperty() {
        return purchasedItemsValidity;
    }

    public double getSum() {
        return sum.get();
    }

    public ReadOnlyDoubleProperty sumProperty() {
        return sum;
    }
    //</editor-fold>
}

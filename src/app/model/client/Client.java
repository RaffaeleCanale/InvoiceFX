package app.model.client;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.binding.ListBinding;
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

    private final IntegerProperty clientId = new SimpleIntegerProperty();
    private final BooleanBinding clientIdValidity = clientId.greaterThan(0);

    private final StringProperty clientName = new SimpleStringProperty();
    private final BooleanBinding clientNameValidity = clientName.isNotNull();

    private final ObservableList<PurchasedItem> purchasedItems = FXCollections.observableArrayList();
    private final BooleanBinding purchasedItemsValidity = Bindings.isNotEmpty(purchasedItems);


    private final DoubleProperty sum = new SimpleDoubleProperty();

    public Client() {
        purchasedItems.addListener((ListChangeListener<PurchasedItem>) c -> recomputeTotal());
    }

    public ReadOnlyDoubleProperty sumProperty() {
        return sum;
    }

    private void recomputeTotal() {
        sum.unbind();

        purchasedItems.stream()
                .map(PurchasedItem::sumProperty)
                .reduce(DoubleExpression::add)
                .ifPresent(sum::bind);
    }

    //<editor-fold desc="Getters & Setters" defaultstate="collapsed">
    public int getClientId() {
        return clientId.get();
    }

    public IntegerProperty clientIdProperty() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId.set(clientId);
    }

    public BooleanBinding clientIdValidityProperty() {
        return clientIdValidity;
    }

    public String getClientName() {
        return clientName.get();
    }

    public StringProperty clientNameProperty() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName.set(clientName);
    }

    public BooleanBinding clientNameValidityProperty() {
        return clientNameValidity;
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

    public void setSum(double sum) {
        this.sum.set(sum);
    }
    //</editor-fold>
}

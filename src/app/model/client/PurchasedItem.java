package app.model.client;

import app.model.DateEnabled;
import app.model.item.Item;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;

import java.time.LocalDate;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 15.06.16.
 */
public class PurchasedItem {

    private final ObjectProperty<Client> client = new SimpleObjectProperty<>();
    private final BooleanBinding clientValidity = client.isNotNull();

    private final ObjectProperty<Item> item = new SimpleObjectProperty<>();
    private final BooleanBinding itemValidity = item.isNotNull();

    private final IntegerProperty itemCount = new SimpleIntegerProperty();
    private final BooleanBinding itemCountValidity = itemCount.greaterThan(0);

    private final ObjectProperty<DateEnabled> dateEnabled = new SimpleObjectProperty<>();
    private final BooleanBinding dateEnabledValidity = dateEnabled.isNotNull();

    private final ObjectProperty<LocalDate> fromDate = new SimpleObjectProperty<>();
    private final BooleanBinding fromDateValidity = fromDate.isNotNull().or(dateEnabled.isEqualTo(DateEnabled.NONE));

    private final ObjectProperty<LocalDate> toDate = new SimpleObjectProperty<>();
    private final BooleanBinding toDateValidity = toDate.isNotNull().or(dateEnabled.isNotEqualTo(DateEnabled.BOTH));


    private final DoubleBinding sum;

    public PurchasedItem(Client client, Item item) {
        this.client.setValue(client);
        this.item.setValue(item);
        this.sum = Bindings.selectDouble(this.item, "price").multiply(itemCount);
    }

    //<editor-fold defaultstate="collapsed" desc="Getters & Setters">
    public Client getClient() {
        return client.get();
    }

    public ObjectProperty<Client> clientProperty() {
        return client;
    }

    public void setClient(Client client) {
        this.client.set(client);
    }

    public BooleanBinding clientValidityProperty() {
        return clientValidity;
    }

    public Item getItem() {
        return item.get();
    }

    public ObjectProperty<Item> itemProperty() {
        return item;
    }

    public void setItem(Item item) {
        this.item.set(item);
    }

    public BooleanBinding itemValidityProperty() {
        return itemValidity;
    }

    public int getItemCount() {
        return itemCount.get();
    }

    public IntegerProperty itemCountProperty() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount.set(itemCount);
    }

    public BooleanBinding itemCountValidityProperty() {
        return itemCountValidity;
    }

    public DateEnabled getDateEnabled() {
        return dateEnabled.get();
    }

    public ObjectProperty<DateEnabled> dateEnabledProperty() {
        return dateEnabled;
    }

    public void setDateEnabled(DateEnabled dateEnabled) {
        this.dateEnabled.set(dateEnabled);
    }

    public BooleanBinding dateEnabledValidityProperty() {
        return dateEnabledValidity;
    }

    public LocalDate getFromDate() {
        return fromDate.get();
    }

    public ObjectProperty<LocalDate> fromDateProperty() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate.set(fromDate);
    }

    public BooleanBinding fromDateValidityProperty() {
        return fromDateValidity;
    }

    public LocalDate getToDate() {
        return toDate.get();
    }

    public ObjectProperty<LocalDate> toDateProperty() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate.set(toDate);
    }

    public BooleanBinding toDateValidityProperty() {
        return toDateValidity;
    }

    public Number getSum() {
        return sum.get();
    }

    public DoubleBinding sumProperty() {
        return sum;
    }
    //</editor-fold>
}

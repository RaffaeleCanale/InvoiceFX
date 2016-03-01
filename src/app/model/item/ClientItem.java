package app.model.item;

import app.model.DateEnabled;
import app.util.adapter.LocalDateAdapter;
import app.util.interfaces.ValidationModel;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import java.util.logging.Logger;

/**
 * A {@code ClientItem} models an item purchased by the client. Thus, it is defined by the item itself, but also the
 * amount, dates, client name,... Created on 03/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class ClientItem implements ValidationModel {

    public static ClientItem copyOf(ClientItem item) {
        if (item == null) {
            return null;
        }

        ClientItem copy = new ClientItem();
        copyValues(item, copy);

        return copy;
    }

    public static void copyValues(ClientItem from, ClientItem to) {
        to.setClientName(from.getClientName());
        to.setItem(ItemModel.copyOf(from.getItem()));
        to.setItemCount(from.getItemCount());
        to.setFromDate(from.getFromDate());
        to.setToDate(from.getToDate());
        to.setDateEnabled(from.getDateEnabled());
    }

    private final StringProperty clientName = new SimpleStringProperty();
    private final BooleanBinding clientNameValidity = clientName.isNotNull();

    private final ObjectProperty<ItemModel> item = new SimpleObjectProperty<>();
    private final BooleanBinding itemValidity = item.isNotNull();

    private final IntegerProperty itemCount = new SimpleIntegerProperty();
    private final BooleanBinding itemCountValidity = itemCount.greaterThan(0);

    private final ObjectProperty<DateEnabled> dateEnabled = new SimpleObjectProperty<>();

    private final ObjectProperty<LocalDate> fromDate = new SimpleObjectProperty<>();
    private final BooleanBinding fromDateValidity = fromDate.isNotNull().or(dateEnabled.isEqualTo(DateEnabled.NONE));

    private final ObjectProperty<LocalDate> toDate = new SimpleObjectProperty<>();
    private final BooleanBinding toDateValidity = toDate.isNotNull().or(dateEnabled.isNotEqualTo(DateEnabled.BOTH));


    private final DoubleProperty sum = new SimpleDoubleProperty();

    public ClientItem() {
        item.addListener((observable, oldValue, newValue) -> {
            recomputeTotal();
        });
    }

    public BooleanBinding overallValidityProperty() {
        return fromDateValidity.and(toDateValidity).and(itemValidity).and(itemCountValidity);
    }

    @Override
    public boolean isValid() {
        return overallValidityProperty().get() && item.get().isValid();
    }

    @Override
    public void diagnosis(Logger log) {
        if (!overallValidityProperty().get()) {
            log.warning("Overall validity not respected");
        }

        item.get().diagnosis(log);
    }

    public ReadOnlyDoubleProperty sumProperty() {
        return sum;
    }

    private void recomputeTotal() {
        DoubleBinding sum = item.get() == null ?
                new SimpleDoubleProperty(0.0).add(0.0) :
                item.get().priceProperty().multiply(itemCount);

        this.sum.unbind();
        this.sum.bind(sum);
    }


    /*
        --- CLIENT NAME ---
     */
    public StringProperty clientNameProperty() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName.set(clientName);
    }

    public String getClientName() {
        return clientName.get();
    }

    public BooleanBinding clientNameValidityProperty() {
        return clientNameValidity;
    }

    /*
        --- ITEM MODEL ---
    */
    public ObjectProperty<ItemModel> itemProperty() {
        return item;
    }

    public void setItem(ItemModel itemModel) {
        this.item.set(itemModel);
    }

    public ItemModel getItem() {
        return item.get();
    }

    public BooleanBinding itemValidityProperty() {
        return itemValidity;
    }

    /*
        --- ITEM COUNT ---
     */
    public IntegerProperty itemCountProperty() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount.set(itemCount);
    }

    public int getItemCount() {
        return itemCount.get();
    }

    public BooleanBinding itemCountValidityProperty() {
        return itemCountValidity;
    }


    /*
        --- FROM DATE ---
    */
    public ObjectProperty<LocalDate> fromDateProperty() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate.set(fromDate);
    }

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    public LocalDate getFromDate() {
        return fromDate.get();
    }

    public BooleanBinding fromDateValidityProperty() {
        return fromDateValidity;
    }


    /*
        --- TO DATE ---
     */
    public ObjectProperty<LocalDate> toDateProperty() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate.set(toDate);
    }

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    public LocalDate getToDate() {
        return toDate.get();
    }

    public BooleanBinding toDateValidityProperty() {
        return toDateValidity;
    }


    /*
        --- DATE ENABLED ---
     */
    public ObjectProperty<DateEnabled> dateEnabledProperty() {
        return dateEnabled;
    }

    public void setDateEnabled(DateEnabled dateEnabled) {
        this.dateEnabled.set(dateEnabled);
    }

    public DateEnabled getDateEnabled() {
        return dateEnabled.get();
    }

    @Override
    public String toString() {
        return getClientName() + ": " + getItemCount() + " " + getItem() + " [" +
                (getDateEnabled() != DateEnabled.NONE ? getFromDate() : "-") + " <> " +
                (getDateEnabled() == DateEnabled.BOTH ? getToDate() : "-") + "]";
    }
}

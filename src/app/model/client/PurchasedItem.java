package app.model.client;

import app.model.DateEnabled;
import app.model.item.ItemModel;
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

    private final IntegerProperty purchaseId = new SimpleIntegerProperty();
    private final BooleanBinding purchaseIdValidity = purchaseId.greaterThan(0);

    private final ObjectProperty<ItemModel> item = new SimpleObjectProperty<>();
    private final BooleanBinding itemValidity = item.isNotNull();

    private final IntegerProperty itemCount = new SimpleIntegerProperty();
    private final BooleanBinding itemCountValidity = itemCount.greaterThan(0);

    private final ObjectProperty<DateEnabled> dateEnabled = new SimpleObjectProperty<>();
    private final BooleanBinding dateEnabledValidity = dateEnabled.isNotNull();

    private final ObjectProperty<LocalDate> fromDate = new SimpleObjectProperty<>();
    private final BooleanBinding fromDateValidity = fromDate.isNotNull().or(dateEnabled.isEqualTo(DateEnabled.NONE));

    private final ObjectProperty<LocalDate> toDate = new SimpleObjectProperty<>();
    private final BooleanBinding toDateValidity = toDate.isNotNull().or(dateEnabled.isNotEqualTo(DateEnabled.BOTH));


    private final DoubleBinding sum = Bindings.selectDouble(item, "price").multiply(itemCount);

    //<editor-fold defaultstate="collapsed" desc="Getters & Setters">
    public int getPurchaseId() {
        return purchaseId.get();
    }

    public IntegerProperty purchaseIdProperty() {
        return purchaseId;
    }

    public void setPurchaseId(int purchaseId) {
        this.purchaseId.set(purchaseId);
    }

    public Boolean getPurchaseIdValidity() {
        return purchaseIdValidity.get();
    }

    public BooleanBinding purchaseIdValidityProperty() {
        return purchaseIdValidity;
    }

    public ItemModel getItem() {
        return item.get();
    }

    public ObjectProperty<ItemModel> itemProperty() {
        return item;
    }

    public void setItem(ItemModel item) {
        this.item.set(item);
    }

    public Boolean getItemValidity() {
        return itemValidity.get();
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

    public Boolean getItemCountValidity() {
        return itemCountValidity.get();
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

    public Boolean getDateEnabledValidity() {
        return dateEnabledValidity.get();
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

    public Boolean getFromDateValidity() {
        return fromDateValidity.get();
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

    public Boolean getToDateValidity() {
        return toDateValidity.get();
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

package app.model.invoice;

import app.model.item.ClientItem;
import app.util.adapter.LocalDateAdapter;
import app.model.ValidationModel;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created on 02/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
@XmlRootElement(name = "invoice")
public class InvoiceModel implements ValidationModel {

    public static InvoiceModel copyOf(InvoiceModel model) {
        InvoiceModel copy = new InvoiceModel();
        copyValues(model, copy);

        return copy;
    }

    public static void copyValues(InvoiceModel from, InvoiceModel to) {
        to.setId(from.getId());
        to.setAddress(from.getAddress());
        to.setDate(from.getDate());
        to.setPdfFileName(from.getPdfFileName());

        to.getItems().clear();
        from.getItems().forEach(item -> to.getItems().add(ClientItem.copyOf(item)));
    }


    private final IntegerProperty id = new SimpleIntegerProperty();
    private final BooleanBinding idValidity = id.greaterThanOrEqualTo(1);

    private final StringProperty address = new SimpleStringProperty();
    private final BooleanBinding addressValidity = address.isNotEmpty();

    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>(LocalDate.now());
    private final BooleanBinding dateValidity = date.isNotNull();

    private final ObservableList<ClientItem> items = FXCollections.observableArrayList();
    private final BooleanBinding itemsValidity = Bindings.isNotEmpty(items);

    private final StringProperty pdfFileName = new SimpleStringProperty();

    private final DoubleProperty sumProperty = new SimpleDoubleProperty();

    public InvoiceModel() {
        items.addListener((ListChangeListener<ClientItem>) c -> recomputeTotal());
    }

    @Override
    public boolean isValid() {
        return idValidity.get() &&
                addressValidity.get() &&
                dateValidity.get() &&
                itemsValidity.get() &&
                items.stream().allMatch(ClientItem::isValid);
    }

    @Override
    public void diagnosis(Logger log) {
        Map<BooleanBinding, String> names = new HashMap<>();
        names.put(idValidity, "ID");
        names.put(addressValidity, "Address");
        names.put(dateValidity, "Date");
        names.put(itemsValidity, "Items");

        names.forEach((b,n) -> {
            if (!b.get()) {
                log.warning("Field '" + n + "' is not valid");
            }
        });
        items.forEach(i -> i.diagnosis(log));
    }

    public ReadOnlyDoubleProperty sumProperty() {
        return ReadOnlyDoubleProperty.readOnlyDoubleProperty(sumProperty);
    }

    private void recomputeTotal() {
        DoubleBinding sumBinding = items.stream()
                .map((itemModel) -> itemModel.sumProperty().add(0))
                .reduce(DoubleExpression::add)
                .orElse(new SimpleDoubleProperty(0).add(0));

        sumProperty.unbind();
        sumProperty.bind(sumBinding);
    }

    /*
        --- ID ---
     */
    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public int getId() {
        return id.get();
    }

    public BooleanBinding idValidityProperty() {
        return idValidity;
    }


    /*
        --- ADDRESS ---
     */
    public StringProperty addressProperty() {
        return address;
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public String getAddress() {
        return address.get();
    }

    public BooleanBinding addressValidityProperty() {
        return addressValidity;
    }

    /*
        --- DATE ---
     */
    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    public LocalDate getDate() {
        return date.get();
    }

    public BooleanBinding dateValidityProperty() {
        return dateValidity;
    }


    /*
        --- ITEMS ---
     */
    public void setItems(ObservableList<ClientItem> items) {
        this.items.setAll(items);
    }

    public ObservableList<ClientItem> getItems() {
        return items;
    }

    public BooleanBinding itemsValidityProperty() {
        return itemsValidity;
    }


    /*
        --- FILE NAME ---
     */
    public StringProperty getPdfFileNameProperty() {
        return pdfFileName;
    }

    public void setPdfFileName(String pdfFileName) {
        this.pdfFileName.set(pdfFileName);
    }

    public String getPdfFileName() {
        return pdfFileName.get();
    }
}

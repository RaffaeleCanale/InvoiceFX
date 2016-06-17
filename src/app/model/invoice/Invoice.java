package app.model.invoice;

import app.model.client.Client;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.time.LocalDate;

/**
 * Created on 02/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class Invoice {

    private final LongProperty id = new SimpleLongProperty();
    private final BooleanBinding idValidity = id.greaterThan(0);

    private final StringProperty address = new SimpleStringProperty();
    private final BooleanBinding addressValidity = address.isNotEmpty();

    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>(LocalDate.now());
    private final BooleanBinding dateValidity = date.isNotNull();

    private final ObservableList<Client> clients = FXCollections.observableArrayList();
    private final BooleanBinding clientsValidity = Bindings.isNotEmpty(clients);

    private final StringProperty pdfFileName = new SimpleStringProperty();
    private final BooleanBinding pdfFileNameValidity = pdfFileName.isNotEmpty();

    private final DoubleProperty sum = new SimpleDoubleProperty();

    public Invoice() {
        clients.addListener((ListChangeListener<Client>) c -> recomputeTotal());
    }

    private void recomputeTotal() {
        sum.unbind();

        clients.stream()
                .map(i -> (DoubleExpression) i.sumProperty())
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

    public String getAddress() {
        return address.get();
    }

    public StringProperty addressProperty() {
        return address;
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public BooleanBinding addressValidityProperty() {
        return addressValidity;
    }

    public LocalDate getDate() {
        return date.get();
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    public BooleanBinding dateValidityProperty() {
        return dateValidity;
    }

    public ObservableList<Client> getClients() {
        return clients;
    }

    public BooleanBinding clientsValidityProperty() {
        return clientsValidity;
    }

    public String getPdfFileName() {
        return pdfFileName.get();
    }

    public StringProperty pdfFileNameProperty() {
        return pdfFileName;
    }

    public void setPdfFileName(String pdfFileName) {
        this.pdfFileName.set(pdfFileName);
    }

    public BooleanBinding pdfFileNameValidityProperty() {
        return pdfFileNameValidity;
    }

    public double getSum() {
        return sum.get();
    }

    public ReadOnlyDoubleProperty sumProperty() {
        return ReadOnlyDoubleProperty.readOnlyDoubleProperty(sum);
    }
    //</editor-fold>
}

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
public class InvoiceModel {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final BooleanBinding idValidity = id.greaterThan(0);

    private final StringProperty address = new SimpleStringProperty();
    private final BooleanBinding addressValidity = address.isNotEmpty();

    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>(LocalDate.now());
    private final BooleanBinding dateValidity = date.isNotNull();

    private final ObservableList<Client> clients = FXCollections.observableArrayList();
    private final BooleanBinding clientsValidity = Bindings.isNotEmpty(clients);

    private final StringProperty pdfFileName = new SimpleStringProperty();
    private final BooleanBinding pdfFileNameValidity = pdfFileName.isNotEmpty();

    private final DoubleProperty sumProperty = new SimpleDoubleProperty();

    public InvoiceModel() {
        clients.addListener((ListChangeListener<Client>) c -> recomputeTotal());
    }

    public ReadOnlyDoubleProperty sumProperty() {
        return ReadOnlyDoubleProperty.readOnlyDoubleProperty(sumProperty);
    }

    private void recomputeTotal() {
        sumProperty.unbind();

        clients.stream()
                .map(i -> (DoubleExpression) i.sumProperty())
                .reduce(DoubleExpression::add)
                .ifPresent(sumProperty::bind);
    }

}

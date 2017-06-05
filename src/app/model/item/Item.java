package app.model.item;

import app.model.DateEnabled;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created on 02/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
@XmlRootElement
public class Item {

    private final LongProperty id = new SimpleLongProperty();
    private final BooleanBinding idValidity = id.greaterThan(0);

    private final StringProperty name = new SimpleStringProperty();
    private final BooleanBinding nameValidity = name.isNotEmpty();

    private final DoubleProperty price = new SimpleDoubleProperty();
    private final BooleanBinding priceValidity = new BooleanBinding() {
        @Override
        protected boolean computeValue() {
            return true;
        }
    };

    private final DoubleProperty vat = new SimpleDoubleProperty();
    private final BooleanBinding vatValidity = vat.greaterThanOrEqualTo(0).and(vat.lessThanOrEqualTo(100));

    private final ObjectProperty<DateEnabled> defaultDateEnabled = new SimpleObjectProperty<>();
    private final BooleanBinding defaultDateEnabledValidity = defaultDateEnabled.isNotNull();


    //<editor-fold desc="Getters & Setters" defaultstate="collpased">
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

    public double getPrice() {
        return price.get();
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public BooleanBinding priceValidityProperty() {
        return priceValidity;
    }

    public double getVat() {
        return vat.get();
    }

    public DoubleProperty vatProperty() {
        return vat;
    }

    public void setVat(double vat) {
        this.vat.set(vat);
    }

    public BooleanBinding vatValidityProperty() {
        return vatValidity;
    }

    public DateEnabled getDefaultDateEnabled() {
        return defaultDateEnabled.get();
    }

    public ObjectProperty<DateEnabled> defaultDateEnabledProperty() {
        return defaultDateEnabled;
    }

    public void setDefaultDateEnabled(DateEnabled defaultDateEnabled) {
        this.defaultDateEnabled.set(defaultDateEnabled);
    }

    public BooleanBinding defaultDateEnabledValidityProperty() {
        return defaultDateEnabledValidity;
    }
    //</editor-fold>
}

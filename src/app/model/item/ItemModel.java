package app.model.item;

import app.model.DateEnabled;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import app.util.interfaces.ValidationModel;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.logging.Logger;

/**
 * Created on 02/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
@XmlRootElement
public class ItemModel implements ValidationModel {

    public static ItemModel copyOf(ItemModel model) {
        if (model == null) {
            return null;
        }

        ItemModel copy = new ItemModel();
        copyValues(model, copy);

        return copy;
    }

    public static void copyValues(ItemModel from, ItemModel to) {
        to.setItemName(from.getItemName());
        to.setPrice(from.getPrice());
        to.setVat(from.getVat());
        to.setDefaultDateEnabled(from.getDefaultDateEnabled());
    }


    private final StringProperty itemName = new SimpleStringProperty();
    private final BooleanBinding itemNameValidity = itemName.isNotNull();

    private final DoubleProperty price = new SimpleDoubleProperty();
    private final BooleanBinding priceValidity = price.greaterThanOrEqualTo(0);

    private final DoubleProperty vat = new SimpleDoubleProperty();
    private final BooleanBinding vatValidity = vat.greaterThanOrEqualTo(0).and(vat.lessThanOrEqualTo(100));

    private final ObjectProperty<DateEnabled> defaultDateEnabled = new SimpleObjectProperty<>();

    public BooleanBinding overallValidity() {
        return itemNameValidity.and(priceValidity).and(vatValidity);
    }

    @Override
    public boolean isValid() {
        return overallValidity().get();
    }

    @Override
    public void diagnosis(Logger log) {
        if (!overallValidity().get()) {
            log.warning("Overall validity not respected");
        }
    }

    /*
            --- ITEM NAME ---
         */
    public StringProperty itemNameProperty() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName.set(itemName);
    }

    public String getItemName() {
        return itemName.get();
    }

    public BooleanBinding itemNameValidityProperty() {
        return itemNameValidity;
    }

    /*
        --- PRICE ---
     */
    public DoubleProperty priceProperty() {
        return price;
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public double getPrice() {
        return price.get();
    }

    public BooleanBinding priceValidityProperty() {
        return priceValidity;
    }


    /*
        --- VAT ---
     */
    public DoubleProperty vatProperty() {
        return vat;
    }

    public void setVat(double vat) {
        this.vat.set(vat);
    }

    public double getVat() {
        return vat.get();
    }

    public BooleanBinding vatValidityProperty() {
        return vatValidity;
    }

    /*
        --- DATE ENABLED ---
     */
    public ObjectProperty<DateEnabled> defaultDateEnabledProperty() {
        return defaultDateEnabled;
    }

    public void setDefaultDateEnabled(DateEnabled defaultDateEnabled) {
        this.defaultDateEnabled.set(defaultDateEnabled);
    }

    public DateEnabled getDefaultDateEnabled() {
        return defaultDateEnabled.get();
    }

    @Override
    public String toString() {
        return getItemName() + " " + getPrice() + " (" + getVat() + "%)";
    }
}

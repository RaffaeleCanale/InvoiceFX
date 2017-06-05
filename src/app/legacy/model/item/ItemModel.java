package app.legacy.model.item;

import app.legacy.model.DateEnabled;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import app.legacy.model.ValidationModel;

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
        to.setTva(from.getTva());
        to.setDefaultDateEnabled(from.getDefaultDateEnabled());
    }


    private final StringProperty itemName = new SimpleStringProperty();
    private final BooleanBinding itemNameValidity = itemName.isNotNull();

    private final DoubleProperty price = new SimpleDoubleProperty();
    private final BooleanBinding priceValidity = price.greaterThanOrEqualTo(0);

    private final DoubleProperty tva = new SimpleDoubleProperty();
    private final BooleanBinding tvaValidity = tva.greaterThanOrEqualTo(0).and(tva.lessThanOrEqualTo(100));

    private final ObjectProperty<DateEnabled> defaultDateEnabled = new SimpleObjectProperty<>();

    public BooleanBinding overallValidity() {
        return itemNameValidity.and(priceValidity).and(tvaValidity);
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
    public DoubleProperty tvaProperty() {
        return tva;
    }

    public void setTva(double tva) {
        this.tva.set(tva);
    }

    public double getTva() {
        return tva.get();
    }

    public BooleanBinding tvaValidityProperty() {
        return tvaValidity;
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
        return getItemName() + " " + getPrice() + " (" + getTva() + "%)";
    }
}

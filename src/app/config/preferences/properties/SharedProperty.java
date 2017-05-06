package app.config.preferences.properties;

import java.util.Locale;

import static app.config.preferences.properties.PropertyCore.*;

/**
 * List of all properties that are synchronized.
 * <p>
 * Created on 15/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public enum SharedProperty implements ConfigProperty {
    ID_FORMAT(stringProperty("000000")),
    MONEY_FORMAT(stringProperty("Sfr. %.2f")),
    MONEY_DECIMAL_FORMAT(stringProperty("'Sfr.' #0.00")),
    VAT_DECIMAL_FORMAT(stringProperty("#0.# '%'")),
    LANGUAGE(stringProperty(Locale.getDefault().toLanguageTag())),
    //    PDF_FILE_NAME(stringProperty("$id_$client[0].name[1]")),
    ARCHIVES_DEFAULT_ACTION(intProperty(0)),
    DATE_PATTERN(stringProperty("dd/MM/yyyy")),
    SHOW_ITEM_COUNT(booleanProperty(true)),
    EURO_TO_CHF_CURRENCY(doubleProperty(1.08667311)),
    INVERT_CURRENCY_DIRECTION(booleanProperty(false)),
    VAT_ROUND(booleanProperty(true)),
    VAT(doubleArrayProperty(3.8, 8));

    private final PropertyCore core;

    SharedProperty(PropertyCore core) {
        this.core = core;
    }

    @Override
    public PropertyCore core() {
        return core;
    }
}

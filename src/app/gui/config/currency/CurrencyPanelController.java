package app.gui.config.currency;

import app.App;
import app.config.Config;
import app.util.helpers.InvoiceHelper;
import app.config.preferences.properties.SharedProperty;
import app.currency.ECBRetriever;
import app.currency.Rates;
import com.wx.properties.PropertiesManager;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;

/**
 * Created on 01/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class CurrencyPanelController {

    @FXML
    private Label leftSpinnerLabel;
    @FXML
    private Label rightSpinnerLabel;

    @FXML
    private Label currentRateLabel;
    @FXML
    private Label rateInfoLabel;
    @FXML
    private Spinner rateSpinner;


    private final BooleanProperty invert = Config.sharedPreferences().booleanProperty(SharedProperty.INVERT_CURRENCY_DIRECTION);
    private final DoubleProperty euroToChfProperty = Config.sharedPreferences().doubleProperty(SharedProperty.EURO_TO_CHF_CURRENCY);
    private Rates rates;

    public void initialize() {
        PropertiesManager lang = App.getLang();

        final String euro = lang.getString("currency.euro");
        final String chf = lang.getString("currency.chf");

        final NumberFormat rateFormat = InvoiceHelper.getFormat("#0.0###");

        // SPINNER
        SpinnerValueFactory.DoubleSpinnerValueFactory factory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 10.0, 1.0, 0.1);
        ObjectProperty<Double> spinnerValue = factory.valueProperty();

        bindCurrencySpinner(spinnerValue);
        factory.setConverter(new StringConverter<Double>() {
            @Override public String toString(Double value) {
                // If the specified value is null, return a zero-length String
                if (value == null) {
                    return "";
                }

                return rateFormat.format(value);
            }

            @Override public Double fromString(String value) {
                try {
                    // If the specified value is null or zero-length, return null
                    if (value == null) {
                        return null;
                    }

                    value = value.trim();

                    if (value.length() < 1) {
                        return null;
                    }

                    // Perform the requested parsing
                    return rateFormat.parse(value).doubleValue();
                } catch (ParseException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        rateSpinner.setValueFactory(factory);

        invert.addListener((observable, oldValue, newValue) ->
                bindCurrencySpinner(spinnerValue)
        );


        // LEFT/RIGHT SPINNER TEXT
        StringBinding leftSpinnerText = Bindings.when(invert)
                .then("1 " + chf + " =")
                .otherwise("1 " + euro + " =");
        StringBinding rightSpinnerText = Bindings.when(invert)
                .then(euro)
                .otherwise(chf);
        leftSpinnerLabel.textProperty().bind(leftSpinnerText);
        rightSpinnerLabel.textProperty().bind(rightSpinnerText);

        // CURRENT RATE + INFO
        currentRateLabel.setText(lang.getString("currency.current_rate") + " ...");
        rateInfoLabel.setText("");

        new Thread(() -> {
            try {
                rates = ECBRetriever.instance().retrieveRates();

                String currentEuroToChf = rateFormat.format(rates.getEuroToChf());
                String currentChfToEuro = rateFormat.format(1.0 / rates.getEuroToChf());

                StringConverter<LocalDate> dateConverter = InvoiceHelper.dateConverter();

                StringBinding currencyRate = Bindings.when(invert)
                        .then(lang.getString("currency.current_rate") + " 1 " + chf + " = " + currentChfToEuro + " " + euro)
                        .otherwise(lang.getString("currency.current_rate") + " 1 " + euro + " = " + currentEuroToChf + " " + chf);
                Platform.runLater(() -> {
                    currentRateLabel.textProperty().bind(currencyRate);
                    rateInfoLabel.setText(lang.getString("currency.rate_info", dateConverter.toString(rates.getValidityDate())));
                });

                currentRateLabel.setOnMouseClicked(event -> {
                    euroToChfProperty.unbind();
                    euroToChfProperty.setValue(rates.getEuroToChf());
                    bindCurrencySpinner(spinnerValue);
                });

            } catch (IOException e) {
                Platform.runLater(() -> {
                    currentRateLabel.setText(lang.getString("currency.error"));
                    currentRateLabel.setId("error");
                });
            }
        }).start();

    }

    public void setContext(Stage stage) {
        currentRateLabel.setOnMouseEntered(e -> {
            if (rates != null) {
                stage.getScene().setCursor(Cursor.HAND);
            }
        });
        currentRateLabel.setOnMouseExited(e -> stage.getScene().setCursor(Cursor.DEFAULT));
    }


    public void invertCurrency() {
        BooleanProperty invert = Config.sharedPreferences().booleanProperty(SharedProperty.INVERT_CURRENCY_DIRECTION);
        invert.set(!invert.get());
    }

    public void unbindVariables() {
        euroToChfProperty.unbind();
    }

    private void bindCurrencySpinner(ObjectProperty<Double> spinnerValue) {
        euroToChfProperty.unbind();

        if (invert.get()) {
            DoubleExpression constant_1 = ReadOnlyDoubleProperty.doubleExpression(new SimpleDoubleProperty(1.0));

            spinnerValue.setValue(1.0 / euroToChfProperty.get());
            euroToChfProperty.bind(constant_1.divide(DoubleExpression.doubleExpression(spinnerValue)));

        } else {
            spinnerValue.setValue(euroToChfProperty.getValue());
            euroToChfProperty.bind(spinnerValue);
        }
    }

}

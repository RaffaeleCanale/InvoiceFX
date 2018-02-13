package com.wx.invoicefx.ui.views.settings.cards;

import com.wx.fx.Lang;
import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.config.ExceptionLogger;
import com.wx.invoicefx.config.preferences.shared.SharedProperty;
import com.wx.invoicefx.currency.ECBRetriever;
import com.wx.invoicefx.currency.Rates;
import com.wx.invoicefx.model.InvoiceFormats;
import com.wx.invoicefx.ui.components.NumberTextField;
import com.wx.invoicefx.ui.components.RemoveableComponent;
import com.wx.util.concurrent.Callback;
import com.wx.util.concurrent.ConcurrentUtil;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.function.Function;

import static com.wx.invoicefx.config.preferences.shared.SharedProperty.AUTO_UPDATE_CURRENCY;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.06.17.
 */
public class CurrencyRateController implements RemoveableComponent {

    @FXML
    private BorderPane contentPane;
    @FXML
    private NumberTextField rateField;
    @FXML
    private Label leftLabel;
    @FXML
    private Label rightLabel;

    @FXML
    private Label currentRateLabel;
    @FXML
    private Label rateInfoLabel;

    private final ChangeListener<Boolean> invertChangeListener = (observable, oldValue, newValue) -> updateRateView();

    private final BooleanProperty invert = AppResources.sharedPreferences().booleanProperty(SharedProperty.INVERT_CURRENCY_DIRECTION);
    private final DoubleProperty euroToChfProperty = AppResources.sharedPreferences().doubleProperty(SharedProperty.EURO_TO_CHF_CURRENCY);
    private final BooleanProperty autoUpdateCurrency = AppResources.sharedPreferences().booleanProperty(AUTO_UPDATE_CURRENCY);

    private final String EURO = Lang.getString("stage.settings.card.currency.label.euro");
    private final String CHF = Lang.getString("stage.settings.card.currency.label.chf");

    @FXML
    private void initialize() {
        contentPane.setUserData(this);

        final NumberFormat rateFormat = InvoiceFormats.getNumberFormat("#0.0###");

        // RATE FIELD
        rateField.numberProperty().bindBidirectional(euroToChfProperty);
        rateField.setNumberFormat(rateFormat);

        updateRateView();

        invert.addListener(invertChangeListener);

        rateField.disableProperty().bind(autoUpdateCurrency);


        // CURRENT RATE + INFO
        loadCurrentRate(rateFormat);
    }

    @Override
    public void onRemove() {
        rateField.numberProperty().unbindBidirectional(euroToChfProperty);
        invert.removeListener(invertChangeListener);

        rateField.disableProperty().unbind();
    }

    private void loadCurrentRate(NumberFormat rateFormat) {
        final String CURRENT_RATE = Lang.getString("stage.settings.card.currency.label.current_rate");

        currentRateLabel.textProperty().unbind();
        currentRateLabel.setText(CURRENT_RATE + " ...");
        rateInfoLabel.setText("");

        ConcurrentUtil.executeAsync(ECBRetriever.instance()::retrieveRates, new Callback<Rates>() {

            @Override
            public Void success(Rates rates) {
                String currentEuroToChf = rateFormat.format(rates.getEuroToChf());
                String currentChfToEuro = rateFormat.format(1.0 / rates.getEuroToChf());

                StringConverter<LocalDate> dateConverter = InvoiceFormats.dateConverter();

                StringBinding currencyRate = Bindings.when(invert)
                        .then(CURRENT_RATE + " 1 " + CHF + " = " + currentChfToEuro + " " + EURO)
                        .otherwise(CURRENT_RATE + " 1 " + EURO + " = " + currentEuroToChf + " " + CHF);
                Platform.runLater(() -> {
                    currentRateLabel.textProperty().bind(currencyRate);
                    rateInfoLabel.setText(Lang.getString("stage.settings.card.currency.label.rate_info",
                            dateConverter.toString(rates.getValidityDate())));
                });

                return null;
            }

            @Override
            public Void failure(Throwable ex) {
                ExceptionLogger.logException(ex);

                Platform.runLater(() -> {
                    currentRateLabel.setText(Lang.getString("stage.settings.card.currency.label.retrieve_error"));
                    currentRateLabel.getStyleClass().setAll("text-error");
                });

                return null;
            }
        });
    }

    @FXML
    private void updateRateView() {
        if (invert.get()) {
            leftLabel.setText("1 " + CHF + " =");
            rightLabel.setText(EURO);

            Function<Double, Double> invertFn = r -> 1.0 / r;
            rateField.setNumberView(invertFn, invertFn);
        } else {
            leftLabel.setText("1 " + EURO + " =");
            rightLabel.setText(CHF);

            rateField.setNumberView(Function.identity(), Function.identity());
        }
    }

    @FXML
    private void invertCurrency() {
        invert.set(!invert.get());
    }

}

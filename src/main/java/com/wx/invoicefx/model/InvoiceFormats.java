package com.wx.invoicefx.model;

import com.wx.fx.Lang;
import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.util.string.DateConverter;
import javafx.beans.binding.NumberExpression;
import javafx.beans.binding.StringBinding;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

import static com.wx.invoicefx.config.preferences.shared.SharedProperty.*;

/**
 * Created on 10/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public class InvoiceFormats {

    /**
     * Get the string converter for dates according the user preferences.
     *
     * @return A string converter for dates
     */
    public static StringConverter<LocalDate> dateConverter() {
        return new DateConverter(AppResources.sharedPreferences().getString(DATE_PATTERN));
    }

    /**
     * Get the ID format according to the user preferences.
     *
     * @return A formatter for an invoice ID
     */
    public static DecimalFormat idFormat() {
        return new DecimalFormat(AppResources.sharedPreferences().getString(ID_FORMAT));
    }

    /**
     * Create a string binding of a formatted (according to user preferences) price.
     *
     * @param price Expression for the price
     *
     * @return A formatted string binding of the price
     */
    public static StringBinding formattedPrice(NumberExpression price) {
        String format = AppResources.sharedPreferences().getString(MONEY_FORMAT);
        return price.asString(format);
    }

    /**
     * Get the money format to the user preferences.
     *
     * @return A formatter for money
     */
    public static NumberFormat moneyFormat() {
        return getNumberFormat(AppResources.sharedPreferences().getString(MONEY_DECIMAL_FORMAT));
    }

    /**
     * Get the VAT format according to user preferences.
     *
     * @return A formatter for VAT
     */
    public static NumberFormat vatFormat() {
        return getNumberFormat(AppResources.sharedPreferences().getString(VAT_DECIMAL_FORMAT));
    }

    /**
     * Get a formatter for the given pattern.
     *
     * @param pattern Format pattern
     *
     * @return A formatter for the given pattern
     */
    public static NumberFormat getNumberFormat(String pattern) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');

        return new DecimalFormat(pattern, symbols);
    }

    public static String formatDateTime(Date dateTime) {
        SimpleDateFormat sdf = new SimpleDateFormat(Lang.getString("other.date_format"));
        return sdf.format(dateTime);
    }
}

package com.wx.invoicefx.util.math;

import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.config.preferences.shared.SharedProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 10.06.17.
 */
public class CurrencyUtils {

    public static double roundToTwoPlaces(double value) {
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Convert a CHF sum into a Euro sum using the rate stored in the user preferences.
     * <p>
     * The result is rounded to the cent.
     *
     * @param chf_sum Sum to convertBoxed
     *
     * @return Same sum represented in Euro
     *
     * @see SharedProperty#EURO_TO_CHF_CURRENCY
     */
    public static double computeEuro(double chf_sum) {
        double euro_to_chf = AppResources.sharedPreferences().getDouble(SharedProperty.EURO_TO_CHF_CURRENCY);
        return computeEuro(chf_sum, euro_to_chf);
    }

    public static double computeEuro(double chf_sum, double euro_to_chf) {
        return roundToTwoPlaces( chf_sum / euro_to_chf );
    }

    /**
     * Computes the share for VAT in the given sum.
     * <p>
     * The VAT share is rounded to 1 or 5 cent according to the user preferences.
     *
     * @param vat VAT share percentage (scaled from 0 to 100)
     * @param sum Sum whose share to compute fromF
     *
     * @return Absolute share of the VAT in the given sum
     */
    public static double computeVatShare(double vat, double sum) {
        /*
        The given sum already includes the VAT share, so:

            sum = taxFreeSum + vatSum

        And we have that:

            vatSum = vat * taxFreeSum

        Thus:

            vatSum = vat * (sum - vatSum) = vat*sum - vat*vatSum

            vatSum + vat*vatSum = vat*sum
            vatSum*(1+vat) = vat*sum

            vatSum = vat*sum / (1+vat)

         */
//        boolean roundTo5 = Config.sharedPreferences().getBoolean(SharedProperty.VAT_ROUND);
        double vat_n = vat / 100.0; // Normalize VAT

        double vatSum = vat_n * sum / (1.0 + vat_n);

        return roundToTwoPlaces(vatSum);
    }

    private CurrencyUtils() {
    }
}

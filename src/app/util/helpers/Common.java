package app.util.helpers;

import app.config.Config;
import app.config.preferences.properties.SharedProperty;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 03/11/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class Common {

    public static boolean clearDirectoryFiles(File directory) {
        boolean failed = false;

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        if (!file.delete()) {
                            failed = true;
                        }
                    }
                }
            }
        }


        return failed;
    }

    public static void replaceAll(StringBuilder source, String target, String replacement) {
        int index;
        int lastIndex = -1;

        while ((index = source.indexOf(target, lastIndex)) >= 0) {
            source.replace(index, index + target.length(), replacement);

            lastIndex = index + replacement.length();
        }
    }


    public static double computeEuro(double chf_sum) {
        double euro_to_chf = Config.sharedPreferences().getDoubleProperty(SharedProperty.EURO_TO_CHF_CURRENCY);

        return chf_sum / euro_to_chf;
    }

    public static double computeVatShare(double vat, double sum) {
        boolean roundTo5 = Config.sharedPreferences().getBooleanProperty(SharedProperty.VAT_ROUND);

        double taxFreeSum = sum / (1.0 + vat / 100.0);
        double vatShare = sum - taxFreeSum;


        return roundTo5 ?
                Math.round(vatShare * 20.0) / 20.0 :
                Math.round(vatShare * 100.0) / 100.0;
    }
}

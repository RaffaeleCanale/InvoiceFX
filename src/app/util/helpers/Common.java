package app.util.helpers;

import app.cmd.CommandRunner;
import app.config.Config;
import app.config.preferences.properties.LocalProperty;
import app.config.preferences.properties.SharedProperty;
import app.util.gui.AlertBuilder;
import com.wx.fx.gui.window.StageManager;
import com.wx.fx.transfer.TransferController;
import com.wx.fx.transfer.TransferTask;
import com.wx.fx.util.callback.SimpleCallback;

import java.io.File;

import static com.wx.fx.transfer.TransferTask.Action.MOVE;

/**
 * Common utility methods
 * <p>
 * Created on 03/11/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 */
public class Common {

    /**
     * Open a file using the default system application.
     *
     * @param file File or directory to open
     */
    public static void open(File file) {
        if (!file.exists()) {
            AlertBuilder.error()
                    .key("errors.fnf", file.getAbsolutePath())
                    .show();
            return;
        }

        String launcher = Config.localPreferences().getProperty(LocalProperty.DEFAULT_APP_OPEN, file.getAbsolutePath());
        CommandRunner.getInstance(null, "explorer", launcher).executeInBackground();
    }

    /**
     * Opens a dialog that will move all given files to a given directory.
     *
     * @param files        Files to move
     * @param newDirectory Destination directory
     */
    public static void moveFiles(File[] files, File newDirectory) {
        if (files.length == 0) {
            return;
        }

        StageManager.show(TransferController.STAGE_INFO,
                new TransferTask.Builder()
                        .action(MOVE, files, newDirectory)
                        .build(),
                new SimpleCallback() {
                    @Override
                    public void success(Object[] args) {
                    }

                    @Override
                    public void failure(Throwable ex) {
                        AlertBuilder.error(ex)
                                .key("errors.copy_failed")
                                .show();
                    }
                });
    }

    /**
     * Removes all the files (and only the files) in the given directory.
     * <p>
     * The sub-directories or the files in those sub-directories will not be affected.
     *
     * @param directory Directory to clear
     *
     * @return {@code true} if this operation failed to remove some file(s)
     */
    public static boolean clearDirectoryFiles(File directory) {
        if (!directory.exists()) {
            return false;
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + directory);
        }

        boolean failed = false;

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && !file.delete()) {
                    failed = true;
                }
            }
        }

        return failed;
    }

    /**
     * Replace all occurrences of a target string with a replacement string in the given source.
     *
     * @param source      Source to process
     * @param target      Target string to find and replace
     * @param replacement Replacement for all occurrences of the target string
     */
    public static void replaceAll(StringBuilder source, String target, String replacement) {
        int index;
        int lastIndex = -1;

        while ((index = source.indexOf(target, lastIndex)) >= 0) {
            source.replace(index, index + target.length(), replacement);

            lastIndex = index + replacement.length();
        }
    }

    /**
     * Convert a CHF sum into a Euro sum using the rate stored in the user preferences.
     * <p>
     * The result is rounded to the cent.
     *
     * @param chf_sum Sum to convert
     *
     * @return Same sum represented in Euro
     *
     * @see SharedProperty#EURO_TO_CHF_CURRENCY
     */
    public static double computeEuro(double chf_sum) {
        double euro_to_chf = Config.sharedPreferences().getDoubleProperty(SharedProperty.EURO_TO_CHF_CURRENCY);

        double euro_sum = chf_sum / euro_to_chf;
        return Math.round(euro_sum * 100.0) / 100.0;
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
     *
     * @see SharedProperty#VAT_ROUND
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
        boolean roundTo5 = Config.sharedPreferences().getBooleanProperty(SharedProperty.VAT_ROUND);
        double vat_n = vat / 100.0; // Normalize VAT

        double vatSum = vat_n * sum / (1.0 + vat_n);

        return roundTo5 ?
                Math.round(vatSum * 20.0) / 20.0 :
                Math.round(vatSum * 100.0) / 100.0;
    }
}

package com.wx.invoicefx.util;

import com.wx.invoicefx.App;
import com.wx.invoicefx.config.ConfigProperties;
import com.wx.invoicefx.config.preferences.LocalProperty;

import java.io.File;

import static com.wx.invoicefx.config.preferences.LocalProperty.DEFAULT_APP_OPEN;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 12.05.17.
 */
public class DesktopUtils {

    /**
     * Open a file using the default system application.
     *
     * @param file File or directory to open
     */
    public static void open(File file) {
        // TODO: 12.05.17 TODO
//        if (!file.exists()) {
//            AlertBuilder.error()
//                    .key("errors.fnf", file.getAbsolutePath())
//                    .show();
//            return;
//        }
//
//        String launcher = ConfigProperties.localPreferences().getString(DEFAULT_APP_OPEN, file.getAbsolutePath());
//        CommandRunner.getInstance(null, "explorer", launcher).executeInBackground();
    }


    /**
     * Open an web URL in a browser. Precise behaviour depend on the current machine preferences.
     *
     * @param url URL to show
     */
    public static void openUrl(String url) {
        App.app.getHostServices().showDocument(url);
    }


}

package com.wx.invoicefx.util;

import com.wx.invoicefx.App;
import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.command.CommandRunner;
import com.wx.invoicefx.util.view.AlertBuilder;
import com.wx.io.file.FileUtil;
import com.wx.util.concurrent.ConcurrentUtil;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import static com.wx.invoicefx.config.preferences.local.LocalProperty.DEFAULT_APP_OPEN;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 12.05.17.
 */
public class DesktopUtils {

    /**
     * Open a file using the default system application.
     *
     * @param file File or directory to openInvoice
     */
    public static void open(File file) {
        if (!file.exists()) {
            AlertBuilder.error()
                    .key("other.errors.file_not_found", file.getAbsolutePath())
                    .show();
            return;
        }

        String launcher = AppResources.localPreferences().getString(DEFAULT_APP_OPEN, file.getAbsolutePath());
        CommandRunner runner = CommandRunner.getInstance(null, "explorer", launcher);
        ConcurrentUtil.executeAsync(runner::execute, ConcurrentUtil.NO_OP);
    }


    /**
     * Open an web URL in a browser. Precise behaviour depend on the current machine preferences.
     *
     * @param url URL to show
     */
    public static void openUrl(String url) {
        App.app.getHostServices().showDocument(url);
    }

    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            Map<String, String> env = System.getenv();
            if (env.containsKey("COMPUTERNAME"))
                return env.get("COMPUTERNAME");
            else if (env.containsKey("HOSTNAME"))
                return env.get("HOSTNAME");
            else
                return "Unknown Computer";
        }
    }

    public static void deleteDirContent(File dir) {
        if (!dir.isDirectory()) {
            return;
        }

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    FileUtil.deleteDir(file);
                } else {
                    file.delete();
                }
            }
        }
    }


    private DesktopUtils() {}

}

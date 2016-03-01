package app.util.gui;

import app.cmd.CommandRunner;
import app.config.Config;
import app.config.preferences.properties.LocalProperty;

import java.io.File;

/**
 * Created on 21/04/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class DesktopOpen {

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

}

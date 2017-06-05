package com.wx.invoicefx;

import com.wx.fx.Lang;
import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.config.Places;
import com.wx.invoicefx.config.ConfigProperties;
import com.wx.invoicefx.config.ExceptionLogger;
import com.wx.invoicefx.google.DriveManager;
import com.wx.io.file.FileUtil;
import com.wx.util.log.LogHelper;
import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.prefs.InvalidPreferencesFormatException;

import static com.wx.invoicefx.config.Places.Dirs.GOOGLE_DIR;
import static com.wx.invoicefx.config.Places.Files.GOOGLE_CREDENTIALS_FILE;
import static com.wx.invoicefx.config.preferences.LocalProperty.DRIVE_CURRENT_USER;
import static com.wx.invoicefx.config.preferences.LocalProperty.LANGUAGE;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 11.05.17.
 */
public class Bootstrap {

    private enum Step {
        INIT_LOGGER {
            @Override
            boolean execute() {
                LogHelper.setupLogger(LogHelper.consoleHandlerShort(Level.ALL));
                return true;
            }
        },

        LOAD_UI_RESOURCES {
            @Override
            boolean execute() {
                StageManager.setStyleSheet(App.class.getResource("/style.css").toExternalForm());
                StageManager.setAppIcon(new Image(App.class.getResourceAsStream("/icons/icon.png")));

                String tag = ConfigProperties.localPreferences().getString(LANGUAGE);

                Lang.setLocale(tag, App.supportedLanguages());
                Lang.initLanguageResource("text");

                return true;
            }
        },

        CREATE_FOLDER_STRUCTURE {
            @Override
            boolean execute() {
                try {
                    Places.init();
                } catch (IOException e) {
                    // TODO: 11.05.17 Error handling
                    ExceptionLogger.logException(e);

                    return false;
                }

                try {
                    for (Places.Dirs dir : Places.Dirs.values()) {
                        File directory = Places.getDir(dir);

                        FileUtil.autoCreateDirectories(directory);
                    }
                } catch (IOException e) {
                    // TODO: 11.05.17 Error handling
                    ExceptionLogger.logException(e);

                    return false;
                }

                return true;
            }
        },

        INIT_FACTORIES {
            @Override
            boolean execute() {
                try {
                    DriveManager.init(Places.getFile(GOOGLE_CREDENTIALS_FILE),
                            ConfigProperties.localPreferences().stringProperty(DRIVE_CURRENT_USER));
                } catch (GeneralSecurityException | IOException e) {
                    // TODO: 11.05.17 Error handling
                    ExceptionLogger.logException(e);

                    return false;
                }

                return true;
            }
        },

        SYNC_ECB {
            @Override
            boolean execute() {
                return true;
            }
        },

        SYNC_DRIVE {
            @Override
            boolean execute() {
                return true;
            }
        },

        LOAD_SHARED_PREFERENCES {
            @Override
            boolean execute() {
                try {
                    ConfigProperties.loadPreferences();
                } catch (IOException | InvalidPreferencesFormatException e) {
                    // TODO: 11.05.17 Error handling
                    ExceptionLogger.logException(e);

                    return false;
                }

                return true;
            }
        };

        abstract boolean execute();
    }


    public static boolean bootstrap() {
        for (Step step : Step.values()) {
            if (!step.execute()) {
                return false;
            }
        }

        return true;
    }

    public static boolean bootstrapWithoutUI() {
        for (Step step : Step.values()) {
            if (step != Step.LOAD_UI_RESOURCES && !step.execute()) {
                return false;
            }
        }

        return true;
    }

}

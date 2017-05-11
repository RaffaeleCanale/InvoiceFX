package com.wx.invoicefx.config;

import com.wx.fx.preferences.UserPreferences;
import com.wx.invoicefx.config.preferences.LocalProperty;
import com.wx.invoicefx.config.preferences.SharedProperty;

import java.io.File;
import java.io.IOException;
import java.util.prefs.InvalidPreferencesFormatException;

import static com.wx.invoicefx.config.ConfigDirectory.Files.EXPORTED_PREFERENCES_FILE;
import static com.wx.invoicefx.config.ConfigDirectory.getConfigFile;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 11.05.17.
 */
public class ConfigPreferences {

//        public enum TexHandler {
//            PORTABLE_INSTALLATION,
//            SYSTEM_CMD,
//            ONLINE
//        }


//        private static final Logger LOG = LogHelper.getLogger(ConfigPreferences.class);

        private static final UserPreferences<LocalProperty> localPreferences =
                new UserPreferences<>(LocalProperty.class);
        private static final UserPreferences<SharedProperty> sharedPreferences =
                new UserPreferences<>(SharedProperty.class);


        public static void initTexHandler() throws IOException {

        }

        /**
         * Load all preferences, synchronizing from Google Drive if necessary
         */
        public static void loadPreferences() {
            File prefs = getConfigFile(EXPORTED_PREFERENCES_FILE);



            try {
                sharedPreferences.loadFromFile(prefs);
            } catch (IOException | InvalidPreferencesFormatException e) {
                ExceptionLogger.logException(e);
            }

//            DriveConfigHelper.performAction(UPDATE, params -> {
//                boolean updated = (boolean) params[0];
//                if (updated) {
//                    LOG.info("Importing Drive preferences");
//                    try {
//                        sharedPreferences.loadFromFile(prefs);
//                    } catch (IOException | InvalidPreferencesFormatException e) {
//                        ExceptionLogger.logException(e);
//                        LOG.severe("Importation failed: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
//                    }
//                }
//            }, prefs);
        }

        /**
         * Save the shared preferences (if changed) to Google Drive
         */
        public static void saveSharedPreferences() {
            if (sharedPreferences.propertiesChanged()) {
                File prefs = getConfigFile(EXPORTED_PREFERENCES_FILE);
//                LOG.info("Saving shared preferences at: " + prefs.getAbsolutePath());

                try {
                    sharedPreferences.saveToFile(prefs);
//                    DriveConfigHelper.performAction(DriveConfigHelper.Action.INSERT, null, prefs);
                } catch (IOException e) {
                    ExceptionLogger.logException(e);
                }
            }
        }

        /**
         * Return the local preferences. See {@link LocalProperty}.
         *
         * @return The local preferences
         */
        public static UserPreferences<LocalProperty> localPreferences() {
            return localPreferences;
        }

        /**
         * Return the shared preferences. See {@link SharedProperty}.
         *
         * @return The shared preferences
         */
        public static UserPreferences<SharedProperty> sharedPreferences() {
            return sharedPreferences;
        }


        private ConfigPreferences() {
        }

    }

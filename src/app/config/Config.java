package app.config;

import app.config.preferences.LocalProperty;
import app.config.preferences.SharedProperty;
import app.google.DriveConfigHelper;
import app.legacy.config.ModelManagerFactory;
import app.legacy.config.manager.ModelManager;
import app.util.ExceptionLogger;
import com.wx.fx.preferences.UserPreferences;
import com.wx.io.AccessorUtil;
import com.wx.io.file.FileUtil;
import com.wx.util.log.LogHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import java.util.prefs.InvalidPreferencesFormatException;

import static app.config.Config.Files.EXPORTED_PREFERENCES_FILE;
import static app.google.DriveConfigHelper.Action.UPDATE;

/**
 * This is the main configuration manager. This class manages the {@link ModelManager}s and the user properties.
 * <p>
 * To use the configuration, it must first be initialized ({@link #initConfig(ModelManagerFactory.Impl)}), then, the managers and preferences
 * can be loaded ({@link #loadManagers()}, {@link #loadPreferences()}).
 * <p>
 * <p>
 * Created on 03/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public class Config {

    public enum TexHandler {
        PORTABLE_INSTALLATION,
        SYSTEM_CMD,
        ONLINE
    }

    public enum Files {
        EXPORTED_PREFERENCES_FILE("preferences.xml"),
        GOOGLE_CREDENTIALS_FILE("credentials"),
        LOGS_DIR("Logs"),
        LATEX_BUILD("Latex_Build"),
        TEMPLATE_DIR("Template")
        ;

        private final String path;

        Files(String path) {
            this.path = path;
        }
    }

    private static final Logger LOG = LogHelper.getLogger(Config.class);
    private static final String CONFIG_DIR_NAME = "Config";
    // TODO: 3/15/16 Add all Config-related constants here! (eg. update, managers locations, etc...)

    private static File configDir;
    private static final UserPreferences<LocalProperty> localPreferences =
            new UserPreferences<>(LocalProperty.class);
    private static final UserPreferences<SharedProperty> sharedPreferences =
            new UserPreferences<>(SharedProperty.class);


//    private static final Map<Double, ObservableList<ItemModel>> itemGroups = new HashMap<>();

    /**
     * Initialize this manager, this must be the first method called.
     *
     * @throws IOException If the config directory is not found or cannot be created
     */
    public static void initConfig() throws IOException {
        configDir = loadConfigDirectory(CONFIG_DIR_NAME);
        LOG.info("Configuration directory located: " + configDir.getAbsolutePath());

    }

    public static void initTexHandler() throws IOException {

    }

    /**
     * Load all preferences, synchronizing from Google Drive if necessary
     */
    public static void loadPreferences() {
        File prefs = getConfigFile(EXPORTED_PREFERENCES_FILE);

        DriveConfigHelper.performAction(UPDATE, params -> {
            boolean updated = (boolean) params[0];
            if (updated) {
                LOG.info("Importing Drive preferences");
                try {
                    sharedPreferences.loadFromFile(prefs);
                } catch (IOException | InvalidPreferencesFormatException e) {
                    ExceptionLogger.logException(e);
                    LOG.severe("Importation failed: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
                }
            }
        }, prefs);
    }

    /**
     * Save the shared preferences (if changed) to Google Drive
     */
    public static void saveSharedPreferences() {
        if (sharedPreferences.propertiesChanged()) {
            File prefs = getConfigFile(EXPORTED_PREFERENCES_FILE);
            LOG.info("Saving shared preferences at: " + prefs.getAbsolutePath());

            try {
                sharedPreferences.saveToFile(prefs);
                DriveConfigHelper.performAction(DriveConfigHelper.Action.INSERT, null, prefs);
            } catch (IOException e) {
                ExceptionLogger.logException(e);
                LOG.severe("Saving failed: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
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



    /**
     * Get a file contained in the config directory.
     *
     * @param name Name (or sub-path) of the file
     *
     * @return File in the config directory
     */
    public static File getConfigFile(Files configFile, String... subFile) {
        File resultFile;

        if (subFile != null && subFile.length > 1) {
            String path = configFile.path + File.separator + String.join(File.separator, subFile);
            resultFile = new File(configDir, path);
        } else {
            resultFile = new File(configDir, configFile.path);
        }

        AccessorUtil.createParent(resultFile);
        return resultFile;
    }

    /**
     * Get a file contained in the config directory, ensuring that this file does not exist.
     *
     * @param name      Name of the file
     * @param extension Extension of the file
     *
     * @return File in the config directory
     */
    public static File getFreshConfigFile(Files configFile, String name, String extension) {
        String path = configFile.path + File.separator + name;
        return FileUtil.getFreshFile(configDir, path, extension);
    }



    /**
     * Return the config directory.
     *
     * @return The config directory
     */
    public static File getConfigDirectory2() {
        return configDir;
    }


    private static File loadConfigDirectory(String path) throws IOException {
        String programDir;
        try {
            programDir = Config.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        File dir = new File(programDir);
        if (dir.isFile()) {
            dir = dir.getParentFile();
        }


        dir = new File(dir, path);
        if (!dir.exists() && !dir.mkdir()) {
            throw new FileNotFoundException(dir.getAbsolutePath());
        }

        return dir;
    }

    /**
     * Find the application launcher, that is, the executable Jar file.
     * <p>
     * If this application has not been compiled in a .jar file, then a {@code RuntimeException} is thrown.
     *
     * @return The executable Jar file
     */
    public static File getAppLauncher() throws IOException {
        try {
            File file = new File(Config.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            if (!file.getName().endsWith(".jar")) {
                throw new IOException("Not a jar file: " + file.getAbsolutePath());
            }
            return file;
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private Config() {
    }

}

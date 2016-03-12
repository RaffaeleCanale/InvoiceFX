package app;

import app.config.Config;
import app.currency.ECBRetriever;
import app.gui.Splash;
import app.util.ExceptionLogger;
import app.util.gui.AlertBuilder;
import com.wx.fx.Lang;
import com.wx.fx.gui.window.StageManager;
import com.wx.fx.util.BundleWrapper;
import com.wx.properties.PropertiesManager;
import com.wx.util.log.LogHelper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static app.config.preferences.properties.SharedProperty.LANGUAGE;

/**
 * Starting point of the App. This class will load all the necessary components and show the main frame when it's ready
 */
public class App extends Application {

    private static final Locale[] SUPPORTED_LANGUAGES = {Locale.FRENCH, Locale.ENGLISH};


    private static final Logger LOG = initMainLog();

    public static final String APPLICATION_NAME = "InvoiceFX";

    // TODO: 3/11/16 See if there is a better way to access the Application (better than just storing it here)
    private static App app;

    public static void main(String[] args) {
        launch(args);
    }

    private static Logger initMainLog() {
        LogHelper.setupLogger(LogHelper.consoleHandlerShort(Level.ALL));
        return LogHelper.getLogger(App.class);
    }

    /**
     * Get the language resource bundle as read-only {@link PropertiesManager}.
     *
     * @return The language resource bundle of this app
     */
    public static PropertiesManager getLang() {
        return new PropertiesManager(new BundleWrapper(ResourceBundle.getBundle("text")));
    }

    /**
     * @return List of Locale available for this App
     */
    public static Locale[] supportedLanguages() {
        return new Locale[]{Locale.FRENCH, Locale.ENGLISH};
    }

    /**
     * Open an web URL in a browser. Precise behaviour depend on the current machine preferences.
     *
     * @param url URL to show
     */
    public static void openUrl(String url) {
        app.getHostServices().showDocument(url);
    }

    /**
     * Load all components of the {@link Config}. Any exception is dealt locally.
     * <p>
     * More specifically, if an exception arises, a dialog is opened with the user to determine whether to retry or
     * ignore the error.
     * <p>
     * This method will return {@code true} if this process is a success. Note that a success is defined by the user
     * intention to continue. For instance, if the loading fails but the user decides to ignore it, then it is
     * considered a success.
     *
     * @return {@code true} if this process was a success, {@code false} if the App should abort and exit
     */
    public static boolean loadConfigSafe() {
        try {
            Config.loadManagers();
            // TODO: 3/11/16 If loadManagers fails, we still should try to loadPreferences!
            Config.loadPreferences();
            return true;

        } catch (IOException e) {
            ExceptionLogger.logException(e);
            int choice = AlertBuilder.error(e)
                    .key("errors.load_config")
                    .button("dialog.retry")
                    .button("dialog.ignore")
                    .button(ButtonType.CANCEL)
                    .show();

            switch (choice) {
                case 0: // Retry
                    return loadConfigSafe();
                case 1: // Ignore
                    // Issue warning about possible data loss
                    return AlertBuilder.warning()
                            .key("errors.load_config.ignore")
                            .button(ButtonType.YES, ButtonType.NO)
                            .show() == 0;
                default: // Cancel
                    return false;
            }
        }
    }

    @Override
    public void start(Stage primaryStage) {
        app = this;

        new Splash().showSplash(primaryStage);

        Thread loadUp = new Thread(() -> {

            StageManager.setStyleSheet(App.class.getResource("/style.css").toExternalForm());
            StageManager.setAppIcon(new Image(App.class.getResourceAsStream("/icons/icon.png")));

            if (!initConfig()) return;
            ECBRetriever.initialize(Config.getConfigFile("Cache"));

            loadUserLocale();

            if (!loadConfigSafe()) return;  // Loads content (invoices, items, ...)

            Platform.runLater(() -> {
                StageManager.show(Stages.OVERVIEW);

                // The primaryStage is not managed by the StageManager, thus must be closed manually
                primaryStage.close();
            });
        });

        loadUp.start();
    }


    private static boolean initConfig() {
        LOG.info("Initialize CONFIG");
        try {
            Config.initConfig();
        } catch (IOException e) {
            // ExceptionLogger needs Config to be used, thus it cannot be used if initConfig fails

            LOG.severe("Failed: " + e.getMessage());
            AlertBuilder.showFatalError(
                    "A fatal error occurred while initializing the configuration",
                    "Here is the stack trace:", e);
            return false;
        }

        return true;
    }

    private static void loadUserLocale() {
        String tag = Config.sharedPreferences().getProperty(LANGUAGE);
        Lang.setLocale(tag, SUPPORTED_LANGUAGES);
    }

}
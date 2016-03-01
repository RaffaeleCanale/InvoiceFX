package app;

import app.config.Config;
import app.config.manager.ModelManager;
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
import updater.util.UpdaterApi;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static app.config.preferences.properties.SharedProperty.LANGUAGE;

public class App extends Application {

    private static final Locale[] SUPPORTED_LANGUAGES = {Locale.FRENCH, Locale.ENGLISH};


    private static final Logger LOG = initMainLog();
    public static final String APPLICATION_NAME = "InvoiceFX";

    private static App app;

    public static void main(String[] args) {
        launch(args);
    }

    private static Logger initMainLog() {
        LogHelper.setupLogger(LogHelper.consoleHandlerShort(Level.ALL));
        return LogHelper.getLogger(App.class);
    }

    public static PropertiesManager getLang() {
        return new PropertiesManager(new BundleWrapper(ResourceBundle.getBundle("text")));
    }

    public static Locale[] supportedLanguages() {
        return new Locale[]{Locale.FRENCH, Locale.ENGLISH};
    }

    public static void openUrl(String url) {
        app.getHostServices().showDocument(url);
    }

    public static void saveSafe(ModelManager<?> manager) {
        try {
            LOG.info("Saving manager");
            manager.save();
        } catch (IOException ex) {
            ExceptionLogger.logException(ex);
            int choice = AlertBuilder.error(ex)
                    .key("errors.saving_failed")
                    .button("dialog.retry")
                    .button("dialog.ignore")
                    .show();

            if (choice == 0) {
                saveSafe(manager);
            }
        }
    }

    public static boolean loadConfigSafe() {
        try {
            Config.loadManagers();
            Config.loadPreferences();
        } catch (IOException e) {
            ExceptionLogger.logException(e);
            int choice = AlertBuilder.error(e)
                    .key("errors.load_config")
                    .button("dialog.retry")
                    .button("dialog.ignore")
                    .button(ButtonType.CANCEL)
                    .show();
            switch (choice) {
                case 0:
                    return loadConfigSafe();
                case 1:
                    int confirm = AlertBuilder.warning()
                            .key("errors.load_config.ignore")
                            .button(ButtonType.YES, ButtonType.NO)
                            .show();

                    return confirm == 0;
                default:
                    return false;
            }
        }

        return true;
    }

    @Override
    public void start(Stage primaryStage) {
        app = this;

        new Splash().showSplash(primaryStage);

        Thread loadUp = new Thread(() -> {

            StageManager.setStyleSheet(App.class.getResource("/style.css").toExternalForm());
            StageManager.setAppIcon(new Image(App.class.getResourceAsStream("/icons/icon.png")));

            if (!initConfig()) return;  // Enables the Exception Logger

            try {
                ECBRetriever.initialize(Config.getConfigFile("Cache"));
            } catch (IOException e) {
                ExceptionLogger.logException(e);
            }

            loadUserLocale();

//            if (!initLang()) return;    // Enables the use of the resource bundle
            if (!loadConfigSafe()) return;  // Loads content (invoices, items, ...)

            Platform.runLater(() -> {
                StageManager.show(Stages.OVERVIEW);
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
            // Cannot use logger yet
            LOG.severe("Failed: " + e.getMessage());
            AlertBuilder.showFatalError(
                    "A fatal error occurred while initializing the configuration",
                    "Here is the stack trace:", e);
            return false;
        }

        return true;
    }

    public static void loadUserLocale() {
        String tag = Config.sharedPreferences().getProperty(LANGUAGE);
        Lang.setLocale(tag, SUPPORTED_LANGUAGES);
//
//        Locale lang = Locale.forLanguageTag(tag);
//
//        if (lang == null) {
//            lang = Locale.getDefault();
//        }
//
//        for (Locale supported : Lang.supportedLanguages()) {
//            if (lang.getLanguage().equals(supported.getLanguage())) {
//                LOG.info("Setting language " + lang.getDisplayName());
//                Locale.setDefault(lang);
//                return;
//            }
//        }
//
//        lang = Lang.supportedLanguages()[0];
//        Locale.setDefault(lang);
//        LOG.severe("Language not supported, setting default: " + lang);
    }

//    public static boolean initLang() {
//        try {
//            Lang.load();
//            StageManager.setResourceBundle(ResourceBundle.getBundle("text"));
//        } catch (IOException e) {
//            ExceptionLogger.logException(e);
//            AlertBuilder.showFatalError(
//                    "A fatal error occurred while initializing the language resources",
//                    ""
//            );
//            return false;
//        }
//
//        return true;
//    }



}
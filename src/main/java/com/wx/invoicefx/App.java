package com.wx.invoicefx;

import com.wx.util.log.LogHelper;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Starting point of the App. This class will get all the necessary components and show the main frame when it's ready
 */
public class App extends Application {

    public static final String APPLICATION_NAME = "InvoiceFX";

    private static final Logger LOG = initMainLog();

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


    @Override
    public void start(Stage primaryStage) {
        app = this;
    }

}
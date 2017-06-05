package com.wx.invoicefx;

import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.config.Places;
import com.wx.invoicefx.model.save.SaveManager;
import com.wx.invoicefx.view.Stages;
import com.wx.util.log.LogHelper;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.wx.invoicefx.config.Places.Dirs.DATA_DIR;

/**
 * Starting point of the App. This class will get all the necessary components and show the main frame when it's ready
 */
public class App extends Application {

    public static final String APPLICATION_NAME = "InvoiceFX";

    private static final Logger LOG = initMainLog();

    // TODO: 3/11/16 See if there is a better way to access the Application (better than just storing it here)
    public static App app;

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

    @Override
    public void start(Stage primaryStage) {
        app = this;


        if (!Bootstrap.bootstrap()) {
            System.exit(1);
            return;
        }

//        StageManager.show(Stages.INVOICES_ARCHIVE, new SaveManager(Places.getDir(DATA_DIR)));
//        StageManager.show(Stages.DEBUG_VIEW);
//        StageManager.show(Stages.OVERVIEW, new SaveManager(Places.getDir(DATA_DIR)));
    }

}